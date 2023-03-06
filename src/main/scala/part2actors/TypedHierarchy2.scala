package part2actors

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Parent_V2 {

  trait CommandParent
  case class CreateChild(name: String) extends CommandParent
  case class TellChild(name: String, message: String) extends CommandParent

  def apply(): Behavior[CommandParent] = Behaviors.setup { context =>
    //have some internal state to keep track of the child actors
    var offspringMap: Map[String,ActorRef[String]] =  Map()

    Behaviors.receiveMessage { message =>
      message match {
        case CreateChild(name) =>
          val newActorRef = context.spawn(Child_V2(),name)
          offspringMap = offspringMap + (name -> newActorRef)
          Behaviors.same
        case TellChild(name, message) =>
          offspringMap.get(name) match {
            case Some(actor) =>
              context.log.info(s"[parent] Sending message to child actor $name")
              actor ! message
            case None =>
              context.log.error(s"[parent] child actor reference not found for name $name")
          }
          Behaviors.same
      }
    }

  }


  object Child_V2 {
    def apply(): Behavior[String] = Behaviors.receive { (context, message) =>
      message match {
        case message: String =>
          context.log.info(s"[${context.self.path}] Processing message: $message")
          Behaviors.same
      }
    }
  }
}

object TypedHierarchy2 extends App {
  //Setup the parent actor of all
  import Parent_V2._
  val userGuardianBehavior: Behavior[CommandParent] = Behaviors.setup { context =>

    val userDefinedParent = context.spawn(Parent_V2(), "UserDefinedTopActor")

    Behaviors.receiveMessage {
      case message =>
        userDefinedParent ! message
        Behaviors.same
    }
    //Behaviors.empty
  }

  val system = ActorSystem(userGuardianBehavior,"UserGuardianSystemActor")
  system ! CreateChild("John")
  system ! CreateChild("Esther")

  system ! TellChild("John", "Testing actor response")
  system ! TellChild("Esther", "Testing actor response")
  system ! TellChild("Esther", "Testing actor response")

  Thread.sleep(6000)
  system.terminate()
}
