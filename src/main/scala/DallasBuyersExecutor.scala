import org.apache.mesos.Protos._
import org.apache.mesos.{ ExecutorDriver, Executor }

import play.api.libs.ws.ning.NingWSClient
import scala.concurrent.ExecutionContext.Implicits.global

class DallasBuyersExecutor extends Executor {

  var uri: String = _
  var driver: ExecutorDriver = _

  override def shutdown(executorDriver: ExecutorDriver): Unit = {}

  override def disconnected(executorDriver: ExecutorDriver): Unit = {}

  override def killTask(executorDriver: ExecutorDriver, taskID: TaskID): Unit = {}

  override def reregistered(executorDriver: ExecutorDriver, slaveInfo: SlaveInfo): Unit = {}

  override def error(executorDriver: ExecutorDriver, s: String): Unit = {}

  override def frameworkMessage(executorDriver: ExecutorDriver, bytes: Array[Byte]): Unit = {

  }

  override def registered(executorDriver: ExecutorDriver, executorInfo: ExecutorInfo, frameworkInfo: FrameworkInfo, slaveInfo: SlaveInfo): Unit = {
    this.driver = executorDriver
  }

  override def launchTask(executorDriver: ExecutorDriver, taskInfo: TaskInfo): Unit = {

    val wsClient = NingWSClient()
    wsClient
      .url(uri)
      .get()
      .map { wsResponse =>
        {
          driver.sendFrameworkMessage(s"movie $uri returned ${wsResponse.status}".getBytes())
        }
      }

  }

}

object DallasBuyersExecutor {

  def apply(uri: String): DallasBuyersExecutor = {

    val res = new DallasBuyersExecutor()
    res.uri = uri
    res

  }

}