package cards.nine.app.ui.collections

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentStatePagerAdapter}
import android.view.ViewGroup
import cards.nine.app.ui.collections.jobs.{ScrollDown, ScrollType}
import cards.nine.process.commons.models.{Card, Collection}
import cards.nine.process.theme.models.NineCardsTheme
import macroid.{ContextWrapper, Ui}

import scala.collection.mutable

case class CollectionsPagerAdapter(fragmentManager: FragmentManager, var collections: Seq[Collection], startPosition: Int)
  (implicit context: ContextWrapper, theme: NineCardsTheme)
  extends FragmentStatePagerAdapter(fragmentManager) {

  val fragments: mutable.WeakHashMap[Int, CollectionFragment] = mutable.WeakHashMap.empty

  var statuses = CollectionsPagerAdapterStatuses()

  private[this] def firstTimeInStartPosition(position: Int) = (statuses.firstTime, position == startPosition) match {
    case (false, true) =>
      statuses = statuses.copy(firstTime = true)
      true
    case _ => false
  }

  override def getItem(position: Int): Fragment = {
    val fragment = new CollectionFragment
    val bundle = new Bundle()
    bundle.putInt(CollectionFragment.keyPosition, position)
    bundle.putBoolean(CollectionFragment.keyAnimateCards, firstTimeInStartPosition(position))
    bundle.putSerializable(CollectionFragment.keyCollection, collections(position))
    bundle.putInt(CollectionFragment.keyCollectionId, collections(position).id)
    bundle.putString(CollectionFragment.keyScrollType, statuses.scrollType.toString)
    fragment.setArguments(bundle)
    fragment
  }

  override def getCount: Int = collections.length

  override def getPageTitle(position: Int): CharSequence = collections(position).name

  override def instantiateItem(container: ViewGroup, position: Int): AnyRef = {
    val fragment = super.instantiateItem(container, position)
    fragments.put(position, fragment.asInstanceOf[CollectionFragment])
    fragment
  }

  override def destroyItem(container: ViewGroup, position: Int, `object`: scala.Any): Unit = {
    fragments.remove(position)
    super.destroyItem(container, position, `object`)
  }

  def addCardsToCollection(positionCollection: Int, cards: Seq[Card]): Unit = {
    val currentCollection = collections(positionCollection)
    val newCollection = currentCollection.copy(cards = currentCollection.cards ++ cards)
    collections = collections.patch(positionCollection, Seq(newCollection), 1)
  }

  def removeCardFromCollection(positionCollection: Int, cards: Seq[Card]): Unit = {
    val currentCollection = collections(positionCollection)
    val newCollection = currentCollection.copy(cards = currentCollection.cards.filterNot(c => cards.contains(c)))
    collections = collections.patch(positionCollection, Seq(newCollection), 1)
  }

  def updateCardFromCollection(positionCollection: Int, cards: Seq[Card]): Unit = {
    val currentCollection = collections(positionCollection)
    val newCollection = currentCollection.copy(cards = cards)
    collections = collections.patch(positionCollection, Seq(newCollection), 1)
  }

  def updateShareCollectionIdFromCollection(positionCollection: Int, sharedCollectionId: Option[String]): Unit = {
    val currentCollection = collections(positionCollection)
    val newCollection = currentCollection.copy(sharedCollectionId = sharedCollectionId)
    collections = collections.patch(positionCollection, Seq(newCollection), 1)
  }

  def getCurrentFragmentPosition: Option[Int] = fragments collectFirst {
    case (id, fragment) if fragment.isActiveFragment => id
  }

  def getActiveFragment: Option[CollectionFragment] = fragments collectFirst {
    case (_, fragment) if fragment.isActiveFragment => fragment
  }

  def getFragmentByPosition(position: Int): Option[CollectionFragment] = fragments.find(_._1 == position).map(_._2)

  def activateFragment(pos: Int): Unit = fragments foreach {
    case (id, fragment) if id == pos => fragment.setActiveFragment(true)
    case (_, fragment) => fragment.setActiveFragment(false)
  }

  def setScrollType(sType: ScrollType): Unit = statuses = statuses.copy(scrollType = sType)

  def notifyChanged(currentPosition: Int): Ui[_] = {
    val uis = fragments map {
      case (id, fragment) => id match {
        case `currentPosition` =>
          Ui(fragment.setActiveFragmentAndScrollType(activeFragment = true, statuses.scrollType))
        case _ =>
          Ui {
            fragment.setActiveFragment(activeFragment = false)
            fragment.setScrollType(statuses.scrollType)
          }
      }
    }
    Ui.sequence(uis.toSeq: _*)
  }

  def clear(): Unit = fragments.clear()
}

case class CollectionsPagerAdapterStatuses(
  scrollType: ScrollType = ScrollDown,
  firstTime: Boolean = false)