import org.apache.mesos.{ MesosExecutorDriver, ExecutorDriver }

/**
  * Created by roadan on 8/2/16.
  */
object ExecutorLauncher extends App {

  val uri = args(0)
  val executor = DallasBuyersExecutor(uri)
  val driver = new MesosExecutorDriver(executor)
  driver.run()

}
