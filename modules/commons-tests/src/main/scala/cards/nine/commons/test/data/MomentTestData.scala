package cards.nine.commons.test.data

import cards.nine.commons.test.data.CommonValues._
import cards.nine.commons.test.data.MomentValues._
import cards.nine.models.reads.MomentImplicits
import cards.nine.models.types._
import cards.nine.models.{Moment, MomentData, MomentTimeSlot}
import play.api.libs.json.Json
import cards.nine.models.Moment.MomentTimeSlotOps

trait MomentTestData extends WidgetTestData {

  import MomentImplicits._

  def moment(num: Int = 0) =
    Moment(
      id = momentId + num,
      collectionId = Option(momentCollectionId + num),
      timeslot = Json.parse(timeslotJson).as[Seq[MomentTimeSlot]],
      wifi = Seq(wifiSeq(num)),
      bluetooth = Seq(bluetoothSeq(num)),
      headphone = headphone,
      momentType = NineCardsMoment(momentTypeSeq(num)),
      widgets = Option(seqWidgetData))

  val moment: Moment         = moment(0)
  val seqMoment: Seq[Moment] = Seq(moment(0), moment(1), moment(2))

  val momentData: MomentData         = moment.toData
  val seqMomentData: Seq[MomentData] = seqMoment map (_.toData)

  def momentData(infoMoment: (NineCardsMoment, Option[String])) =
    MomentData(
      collectionId = None,
      timeslot = infoMoment._1.toMomentTimeSlot,
      wifi = infoMoment._2.toSeq,
      bluetooth = Seq.empty,
      headphone = false,
      momentType = infoMoment._1)

  val minMomentsWithWifi = Seq(momentData(NineCardsMoment.defaultMoment, None))
  val homeNightMoment    = Seq(momentData(HomeNightMoment, Option("wifi")))

}
