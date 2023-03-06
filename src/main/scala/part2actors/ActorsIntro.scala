package part2actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object ActorsIntro extends App {

  val simpleActorBehavior: Behavior[String] = Behaviors.receiveMessage{(message: String) =>
    println(s"I recevied the message: $message")
    Behaviors.same
  }

  def demoSimpleActor(): Unit = {
    val actorSystem = ActorSystem(simpleActorBehavior,"FirstActor")

    actorSystem ! "Some message"

    Thread.sleep(1000)
    actorSystem.terminate()
  }

  object SimpleActorGeneralSetup {
    def apply(): Behavior[String] = Behaviors.setup { context =>
      //add state, private methods etc

      //First message
      Behaviors.receiveMessage { message =>
        context.log.info(s"[simple actor] I have received $message")
        Behaviors.same
      }
    }
  }

  demoSimpleActor()
  val actorSystem = ActorSystem(SimpleActorGeneralSetup.apply(),"SetupActor")
  actorSystem ! "asdasdas"
}
