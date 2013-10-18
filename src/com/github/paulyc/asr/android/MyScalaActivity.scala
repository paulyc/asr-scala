package com.github.paulyc.asr.android

import akka.actor.{Actor, Props, ActorSystem}
import android.app.Activity
import android.content.Context
import android.os.{Bundle, Message, Looper, Handler}
import android.widget.{TextView, LinearLayout}
import java.util.{TimerTask, Timer}

/**
 * Created by paulyc on 10/17/13.
 */
class MyScalaActivity extends Activity {
  /**
   * Called when the activity is first created.
   */

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val ui = new MyUI(getApplicationContext)
    setContentView(ui)
    Application.uiActor ! Initialize(ui)
  }
}

/**
 * Created by paulyc on 10/15/13.
 */

object Application {
  lazy val actorSystem = ActorSystem("MySystem")
  lazy val uiActor = actorSystem.actorOf(Props[UIActor], name="UIActor")
}

case class Initialize(ui: MyUI)

case class UpdateRequest(ui: MyUI)

object MessageCodes {
  val setText = 1
}

class MyUI(context: Context) extends LinearLayout(context) {
  val view = new TextView(context)
  view.setText("The time is ???")
  addView(view)

  val handler = new Handler(Looper.getMainLooper) {
    override def handleMessage(inputMessage : Message) {
      inputMessage.what match {
        case MessageCodes.setText => view.setText(inputMessage.obj.asInstanceOf[String])
      }
    }
  }

  def setText(str: String) {
    val msg = handler.obtainMessage(MessageCodes.setText, str)
    msg.sendToTarget()
  }
}

class UIActor extends Actor {
  val timer = new Timer

  def receive = {
    case Initialize(ui) => init(ui)
    case UpdateRequest(ui) => update(ui)
  }

  def init(ui: MyUI) {
    timer.scheduleAtFixedRate(new TimerTask {
      override def run() {
        self ! UpdateRequest(ui)
      }
    }, 1000L, 1000L)
  }

  def update(ui: MyUI) {
    val str = "The time is " + System.currentTimeMillis() / 1000
    ui.setText(str)
  }
}