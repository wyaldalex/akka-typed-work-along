package part2actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object WordCounterActor {

  sealed trait WordCounterMessage
  case class RawMessageCounter(text: String) extends WordCounterMessage
  case object CheckCurrentTotal extends WordCounterMessage

  def apply() : Behavior[WordCounterMessage] = Behaviors.setup { context =>
    var total_words = 0

    Behaviors.receiveMessage { message =>
      message match {
        case RawMessageCounter(text) =>
          val current_words = text.split(" ").size
          total_words = total_words + current_words
          context.log.info(s"Received new message with # of words: $current_words")
          context.log.info(s"New Total # of words: $total_words")
          Behaviors.same
        case CheckCurrentTotal =>
          context.log.info(s"Current Total # of words: $total_words")
          Behaviors.same
      }
    }
  }
}

object WordCounterTyped extends App {
  import WordCounterActor._
  val actor_system = ActorSystem(WordCounterActor.apply(),"WordCounterActor")
  val message_template = "This is some message"

  for( i <- 1 to 20) {
    actor_system ! RawMessageCounter(message_template.concat(s" ${i}xyz"))
  }

  actor_system ! CheckCurrentTotal

  Thread.sleep(7000)
  actor_system.terminate()
}
