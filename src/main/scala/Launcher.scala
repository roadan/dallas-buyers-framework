import org.apache.mesos.{ MesosSchedulerDriver, Protos }

/**
  * Created by roadan on 7/23/16.
  */
object Launcher extends App {

  val frameworkBuilder = Protos.FrameworkInfo.newBuilder()
    .setName(s"The dallas buyers framework")
    .setFailoverTimeout(10)
    .setUser("root")

  val scheduler = DallasBuyersScheduler()
  val framework = frameworkBuilder.build
  val driver = new MesosSchedulerDriver(scheduler, framework, "zk://192.168.33.112:2181/mesos")
  driver.run()
}