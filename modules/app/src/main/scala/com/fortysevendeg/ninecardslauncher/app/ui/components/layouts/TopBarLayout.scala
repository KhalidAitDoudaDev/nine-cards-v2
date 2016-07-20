package com.fortysevendeg.ninecardslauncher.app.ui.components.layouts

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.{LayoutInflater, View}
import android.widget.{FrameLayout, TextView}
import com.fortysevendeg.ninecardslauncher.app.ui.commons.CommonsTweak._
import com.fortysevendeg.ninecardslauncher.app.ui.components.widgets.tweaks.TintableImageViewTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.LauncherPresenter
import com.fortysevendeg.ninecardslauncher.app.ui.commons.SnailsCommons._
import com.fortysevendeg.macroid.extras.ViewTweaks._
import com.fortysevendeg.macroid.extras.ViewGroupTweaks._
import com.fortysevendeg.macroid.extras.TextTweaks._
import com.fortysevendeg.macroid.extras.ImageViewTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ImageResourceNamed._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.PositionsUtils
import com.fortysevendeg.ninecardslauncher.app.ui.components.models.{CollectionsWorkSpace, MomentWorkSpace, WorkSpaceType}
import com.fortysevendeg.ninecardslauncher.commons._
import com.fortysevendeg.ninecardslauncher.process.commons.models.Collection
import com.fortysevendeg.ninecardslauncher.process.theme.models._
import com.fortysevendeg.macroid.extras.ResourcesExtras._
import com.fortysevendeg.ninecardslauncher.app.commons.{NineCardsPreferencesValue, ShowClockMoment}
import com.fortysevendeg.ninecardslauncher2.{R, TR, TypedFindView}
import macroid.FullDsl._
import macroid._
import org.joda.time.{DateTime, JodaTimePermission}

class TopBarLayout(context: Context, attrs: AttributeSet, defStyle: Int)
  extends FrameLayout(context, attrs, defStyle)
  with Contexts[View]
  with TypedFindView {

  def this(context: Context) = this(context, javaNull, 0)

  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)

  val preferenceValues = new NineCardsPreferencesValue

  lazy val collectionsSearchPanel = Option(findView(TR.launcher_search_panel))

  lazy val collectionsBurgerIcon = Option(findView(TR.launcher_burger_icon))

  lazy val collectionsGoogleIcon = Option(findView(TR.launcher_google_icon))

  lazy val collectionsMicIcon = Option(findView(TR.launcher_mic_icon))

  lazy val momentContent = Option(findView(TR.launcher_moment_content))

  lazy val momentIcon = Option(findView(TR.launcher_moment_icon))

  lazy val momentText = Option(findView(TR.launcher_moment_text))

  // Lower to API 17
  lazy val momentDigitalClock = Option(findView(TR.launcher_moment_text_digital_clock))

  // API 17 and more
  lazy val momentClock = Option(findView(TR.launcher_moment_text_clock))

  lazy val momentGoogleIcon = Option(findView(TR.launcher_moment_google_icon))

  lazy val momentMicIcon = Option(findView(TR.launcher_moment_mic_icon))

  val collectionWorkspace = LayoutInflater.from(context).inflate(R.layout.collection_bar_view_panel, javaNull)

  val momentWorkspace = LayoutInflater.from(context).inflate(R.layout.moment_bar_view_panel, javaNull)

  val displacement = resGetDimensionPixelSize(R.dimen.shadow_displacement_default)

  val radius = resGetDimensionPixelSize(R.dimen.shadow_radius_default)

  val textTweak = tvShadowLayer(radius, displacement, displacement, resGetColor(R.color.shadow_default))

  ((this <~
    vgAddViews(Seq(momentWorkspace, collectionWorkspace))) ~
    (momentText <~ textTweak) ~
    (momentDigitalClock <~ textTweak) ~
    (momentClock <~ textTweak)).run

  def init(implicit context: ActivityContextWrapper, theme: NineCardsTheme, presenter: LauncherPresenter): Ui[Any] =
    (momentWorkspace <~ vInvisible) ~
      (collectionsSearchPanel <~
        vBackgroundBoxWorkspace(theme.get(SearchBackgroundColor))) ~
      (collectionsBurgerIcon <~
        tivDefaultColor(theme.get(SearchIconsColor)) <~
        tivPressedColor(theme.get(SearchPressedColor)) <~
        On.click(Ui(presenter.launchMenu()))) ~
      (collectionsGoogleIcon <~
        tivDefaultColor(theme.get(SearchGoogleColor)) <~
        tivPressedColor(theme.get(SearchPressedColor)) <~
        On.click(Ui(presenter.launchSearch))) ~
      (collectionsMicIcon <~
        tivDefaultColor(theme.get(SearchIconsColor)) <~
        tivPressedColor(theme.get(SearchPressedColor)) <~
        On.click(Ui(presenter.launchVoiceSearch)))

  def reloadMoment(collection: Collection)(implicit context: ActivityContextWrapper, theme: NineCardsTheme, presenter: LauncherPresenter): Ui[Any] = {
    val resIcon = iconCollectionDetail(collection.icon)
    val showClock = preferenceValues.getBoolean(ShowClockMoment)
    val text = if (showClock) {
      val now = new DateTime()
      val month = resGetString(s"month_${now.getMonthOfYear}") map (month => s" $month ${now.getDayOfMonth},") getOrElse ""
      s"${collection.name},$month"
    } else collection.name
    (momentContent <~
      On.click(goToCollection(collection))) ~
      (momentDigitalClock <~ (if (showClock) vVisible else vGone)) ~
      (momentClock <~ (if (showClock) vVisible else vGone)) ~
      (momentIcon <~
        vBackgroundCollection(collection.themedColorIndex) <~
        ivSrc(resIcon)) ~
      (momentText <~
        tvText(text)) ~
      (momentGoogleIcon <~
        On.click(Ui(presenter.launchSearch))) ~
      (momentMicIcon <~
        On.click(Ui(presenter.launchVoiceSearch)))
  }

  def reloadByType(workSpaceType: WorkSpaceType): Ui[Any] = workSpaceType match {
    case MomentWorkSpace if momentWorkspace.getVisibility == View.INVISIBLE =>
      (collectionWorkspace <~ applyFadeOut()) ~ (momentWorkspace <~ applyFadeIn())
    case CollectionsWorkSpace if collectionWorkspace.getVisibility == View.INVISIBLE =>
      (collectionWorkspace <~ applyFadeIn()) ~ (momentWorkspace <~ applyFadeOut())
    case _ => Ui.nop
  }

  private[this] def goToCollection(collection: Collection)(implicit presenter: LauncherPresenter) = {
    val point = momentIcon map { view =>
      val (x, y) = PositionsUtils.calculateAnchorViewPosition(view)
      new Point(x + (view.getWidth / 2), y + (view.getHeight / 2))
    } getOrElse new Point(0, 0)
    Ui(presenter.goToCollection(Some(collection), point))
  }

}
