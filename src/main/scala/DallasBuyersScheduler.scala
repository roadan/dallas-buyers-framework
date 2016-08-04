import java.util
import java.util.Collections
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

  override def offerRescinded(schedulerDriver: SchedulerDriver, offerID: OfferID): Unit = ???

  override def disconnected(schedulerDriver: SchedulerDriver): Unit = ???

  override def reregistered(schedulerDriver: SchedulerDriver, masterInfo: MasterInfo): Unit = ???

  override def slaveLost(schedulerDriver: SchedulerDriver, slaveID: SlaveID): Unit = ???

  override def error(schedulerDriver: SchedulerDriver, s: String): Unit = ???

  override def statusUpdate(schedulerDriver: SchedulerDriver, taskStatus: TaskStatus): Unit = {

    println(s"status updated: ${taskStatus.getState}")

  }

  override def frameworkMessage(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, bytes: Array[Byte]): Unit = ???

  override def resourceOffers(schedulerDriver: SchedulerDriver, list: util.List[Offer]): Unit = {

    for (offer <- list) {
      //println(offer)

      if (!validateOffer(offer)) {

        // please do this!!!
        schedulerDriver.declineOffer(offer.getId)

      } else {

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

        val commandInfo = CommandInfo.newBuilder
          .setValue(s"wget $movie")

        val task = TaskInfo.newBuilder
          .setName(taskId.getValue)
          .setTaskId(taskId)
          .setSlaveId(offer.getSlaveId)
          .addResources(createScalarResource("cpus", 0.2))
          .addResources(createScalarResource("mem", 128))
          .setCommand(commandInfo)
          .build

        println(s"|--> starting to download $movie")
        schedulerDriver.launchTasks(Collections.singleton(offer.getId), List(task))

      }


    }

  }

  override def registered(schedulerDriver: SchedulerDriver, frameworkID: FrameworkID, masterInfo: MasterInfo): Unit = {

    println("registered")
    this.frameworkId = frameworkId

  }

  override def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, i: Int): Unit = ???

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

    result.downloadQueue.push("http://127.0.0.1:8000/IMG_6052.m4v")
    result.downloadQueue.push("http://127.0.0.1:8000/IMG_6051.m4v")
    result.downloadQueue.push("http://127.0.0.1:8000/IMG_6041.m4v")
    result.downloadQueue.push("http://127.0.0.1:8000/IMG_2228.m4v")

    result

  }

}