package part2actors

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object TypedHierarchy extends App {


  object Parent {
    trait Command
    case class CreateChild(name: String) extends Command
    case class TellChild(message: String) extends Command

    def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
      message match {
        case CreateChild(name) =>
          context.log.info(s"[parent ]Creating child with name $name")
          val childRef = context.spawn(Child(), name)
          active(childRef)
      }
    }

    def active(childRef: ActorRef[String]): Behavior[Command] = Behaviors.receive { (context,message) =>
      message match {
        case TellChild(message) =>
          context.log.info(s"[parent] Sending message $message to child")
          childRef ! message
          Behaviors.same
        case _ =>
          context.log.info(s"[parent] Command not supported")
          Behaviors.same
      }

    }
  }

  object Child {
    def apply(): Behavior[String] = Behaviors.receive { (context, message) =>
      message match {
        case message: String =>
          context.log.info(s"[${context.self.path}] Processing message: $message")
          Behaviors.same
      }
    }
  }

  import Parent._
  val userGuardianBehavior: Behavior[Unit] = Behaviors.setup { context =>
    val parent = context.spawn(Parent(),"parent")
    parent ! CreateChild("RandomChild")
    parent ! TellChild("Testing response from child")

    parent ! CreateChild("RandomChild2")
    parent ! TellChild("Testing response from child")

    Behaviors.empty
  }

  val system = ActorSystem(userGuardianBehavior, "SuperUserGuardian")
  Thread.sleep(7000)
  system.terminate()
}
