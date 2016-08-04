import java.util
import java.util.{ UUID, Collections }
import java.util.concurrent.ConcurrentLinkedDeque

import org.apache.mesos.Protos._
import org.apache.mesos.{ SchedulerDriver, Scheduler }

import scala.collection.JavaConversions._
import scala.collection.convert.Decorators

/**
  * Created by roadan on 7/23/16.
  */
class DallasBuyersScheduler extends Scheduler {

  private var frameworkId: FrameworkID = null
  private val downloadQueue = new ConcurrentLinkedDeque[String]()

  override def offerRescinded(schedulerDriver: SchedulerDriver, offerID: OfferID): Unit = {}

  override def disconnected(schedulerDriver: SchedulerDriver): Unit = {}

  override def reregistered(schedulerDriver: SchedulerDriver, masterInfo: MasterInfo): Unit = {}

  override def slaveLost(schedulerDriver: SchedulerDriver, slaveID: SlaveID): Unit = {}

  override def error(schedulerDriver: SchedulerDriver, s: String): Unit = {

    println(s"error: $s")
  }

  override def statusUpdate(schedulerDriver: SchedulerDriver, taskStatus: TaskStatus): Unit = {

    println(s"status updated: ${taskStatus.getState} ")
    println(taskStatus.getMessage)

  }

  override def frameworkMessage(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, bytes: Array[Byte]): Unit = {

    println("************* incoming message *************")
    println(new String(bytes))
    println("********************************************")

  }

  override def resourceOffers(schedulerDriver: SchedulerDriver, list: util.List[Offer]): Unit = {

    for (offer <- list) {
      //println(offer)

      if (validateOffer(offer)) {

        // in some scenarios we might decline the offer and
        // continue running the scheduler
        if (downloadQueue.isEmpty()) {
          schedulerDriver.declineOffer(offer.getId)
          schedulerDriver.stop()
          System.exit(0)
        }

        //lets download this movie
        val movie = downloadQueue.pop()

        val taskId = TaskID.newBuilder
          .setValue("task_" + System.currentTimeMillis())

        // now our command runs our executor
        val commandInfo = CommandInfo.newBuilder
          .setValue(s"java -cp dallas-buyers-framework-assembly-0.1.0.jar ExecutorLauncher $movie")
          .addUris(CommandInfo.URI.newBuilder
            .setValue("http://192.168.33.112:8000/dallas-buyers-framework-assembly-0.1.0.jar")
            .build())
          .build()

        // configuring our executor
        val executorId = ExecutorID.newBuilder()
          .setValue(UUID.randomUUID.toString)
          .build()

        val executorInfo = ExecutorInfo.newBuilder()
          .setExecutorId(executorId)
          .setFrameworkId(frameworkId)
          .setCommand(commandInfo)
          .build()

        val task = TaskInfo.newBuilder
          .setName(taskId.getValue)
          // now we are telling mesos that the task will run inside our executor
          .setExecutor(executorInfo)
          .setTaskId(taskId)
          .setSlaveId(offer.getSlaveId)
          .addResources(createScalarResource("cpus", 0.2))
          .addResources(createScalarResource("mem", 128))
          .build

        println(s"|--> starting to download $movie")
        schedulerDriver.launchTasks(Collections.singleton(offer.getId), List(task))

      }
      else {

        // please do this!!!
        schedulerDriver.declineOffer(offer.getId)

      }

    }

  }

  override def registered(schedulerDriver: SchedulerDriver, frameworkID: FrameworkID, masterInfo: MasterInfo): Unit = {

    println(s"registered ~~> ${frameworkID}")
    this.frameworkId = frameworkID

  }

  override def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, i: Int): Unit = {

  }

  def validateOffer(offer: Offer): Boolean = {

    val resources = offer.getResourcesList

    resources.count(r => r.getName == "cpus" && r.getScalar.getValue >= 0.1) > 0 &&
      resources.count(r => r.getName == "mem" && r.getScalar.getValue >= 128) > 0
  }

  def createScalarResource(name: String, value: Double): Resource = {
    Resource.newBuilder
      .setName(name)
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder().setValue(value)).build()
  }

}

object DallasBuyersScheduler {

  def apply(): DallasBuyersScheduler = {

    val result = new DallasBuyersScheduler()
    result.downloadQueue.push("http://192.168.33.112:8000/IMG_6052.m4v")
    result.downloadQueue.push("http://192.168.33.112:8000/IMG_6051.m4v")
    result.downloadQueue.push("http://192.168.33.112:8000/IMG_6041.m4v")
    result.downloadQueue.push("http://192.168.33.112:8000/IMG_2228.m4v")

    result

  }

}