import java.util
import java.util.concurrent.ConcurrentLinkedDeque

import org.apache.mesos.Protos._
import org.apache.mesos.{ SchedulerDriver, Scheduler }

import scala.collection.JavaConversions._

/**
  * Created by roadan on 7/23/16.
  */
class DallasBuyersScheduler extends Scheduler {

  private var frameworkId: FrameworkID = null
  private val fileList = new ConcurrentLinkedDeque[String]()

  override def offerRescinded(schedulerDriver: SchedulerDriver, offerID: OfferID): Unit = ???

  override def disconnected(schedulerDriver: SchedulerDriver): Unit = ???

  override def reregistered(schedulerDriver: SchedulerDriver, masterInfo: MasterInfo): Unit = ???

  override def slaveLost(schedulerDriver: SchedulerDriver, slaveID: SlaveID): Unit = ???

  override def error(schedulerDriver: SchedulerDriver, s: String): Unit = ???

  override def statusUpdate(schedulerDriver: SchedulerDriver, taskStatus: TaskStatus): Unit = ???

  override def frameworkMessage(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, bytes: Array[Byte]): Unit = ???

  override def resourceOffers(schedulerDriver: SchedulerDriver, list: util.List[Offer]): Unit = {

    for (offer <- list) {
      println(offer)
      schedulerDriver.declineOffer(offer.getId)
    }

  }

  override def registered(schedulerDriver: SchedulerDriver, frameworkID: FrameworkID, masterInfo: MasterInfo): Unit = {

    println("registered")
    this.frameworkId = frameworkId

  }

  override def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, slaveID: SlaveID, i: Int): Unit = ???

}

object DallasBuyersScheduler {

  def apply(): DallasBuyersScheduler = {

    val result = new DallasBuyersScheduler()

    result.fileList.add("IMG_6052.m4v")
    result.fileList.add("IMG_6051.m4v")
    result.fileList.add("IMG_6041.m4v")
    result.fileList.add("IMG_2228.m4v")

    result

  }

}