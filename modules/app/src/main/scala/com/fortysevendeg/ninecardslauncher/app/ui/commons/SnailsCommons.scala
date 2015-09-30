package com.fortysevendeg.ninecardslauncher.app.ui.commons

import android.animation._
import android.graphics.Color
import android.view.View
import android.view.animation.{DecelerateInterpolator, AccelerateInterpolator, AccelerateDecelerateInterpolator}
import com.fortysevendeg.macroid.extras.ResourcesExtras._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ColorsUtils._
import com.fortysevendeg.ninecardslauncher2.R
import macroid.{Snail, ContextWrapper}

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

object SnailsCommons {

  val defaultDelay = 30

  val noDelay = 0

  def showFabMenu(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()
      view.setScaleX(0)
      view.setScaleY(0)
      view.setVisibility(View.VISIBLE)
      view.animate.
        scaleX(1).
        scaleY(1).
        setInterpolator(new AccelerateDecelerateInterpolator()).
        setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            view.setLayerType(View.LAYER_TYPE_NONE, null)
            animPromise.success()
          }
        }).start()
      animPromise.future
  }

  def hideFabMenu(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()
      view.animate.
        scaleX(0).
        scaleY(0).
        setInterpolator(new AccelerateDecelerateInterpolator()).
        setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            view.setLayerType(View.LAYER_TYPE_NONE, null)
            view.setVisibility(View.GONE)
            animPromise.success()
          }
        }).start()
      animPromise.future
  }

  def animFabMenuItem(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      val duration = resGetInteger(R.integer.anim_duration_normal)
      val translationY = resGetDimensionPixelSize(R.dimen.padding_default)
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()
      view.setVisibility(View.VISIBLE)
      view.setTranslationY(translationY)
      val delay = extractDelay(view)
      view.animate.
        setStartDelay(delay).
        setDuration(duration).
        translationY(0).
        setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            view.setLayerType(View.LAYER_TYPE_NONE, null)
            animPromise.success()
          }
        }).start()
      animPromise.future
  }

  def animFabMenuTitleItem(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      val duration = resGetInteger(R.integer.anim_duration_normal)
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()
      view.setVisibility(View.VISIBLE)
      view.setAlpha(0)
      view.animate.
        setStartDelay(extractDelay(view)).
        setDuration(duration).
        setInterpolator(new AccelerateInterpolator()).
        alpha(1).
        setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            view.setLayerType(View.LAYER_TYPE_NONE, null)
            animPromise.success()
          }
        }).start()
      animPromise.future
  }

  def animFabMenuIconItem(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      val duration = resGetInteger(R.integer.anim_duration_normal)
      val size = resGetDimensionPixelSize(R.dimen.size_fab_menu_item)
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()
      view.setVisibility(View.VISIBLE)
      view.setScaleX(0)
      view.setScaleY(0)
      view.setAlpha(0)
      view.setPivotX(size / 2)
      view.setPivotY(size)
      view.animate.
        setStartDelay(extractDelay(view)).
        setDuration(duration).
        setInterpolator(new DecelerateInterpolator()).
        alpha(1).
        scaleX(1).
        scaleY(1).
        setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            view.setLayerType(View.LAYER_TYPE_NONE, null)
            animPromise.success()
          }
        }).start()
      animPromise.future
  }

  def fadeBackground(color: Int)(implicit context: ContextWrapper): Snail[View] = Snail[View] {
    view =>
      val duration = resGetInteger(R.integer.anim_duration_normal)
      view.clearAnimation()
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      val animPromise = Promise[Unit]()

      val colorFrom = ColorsUtils.setAlpha(color, 0f)
      val colorTo = ColorsUtils.setAlpha(color, 1f)

      val valueAnimator = ValueAnimator.ofInt(0, 100)
      valueAnimator.setDuration(duration)
      valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        override def onAnimationUpdate(value: ValueAnimator) = {
          val color = interpolateColors(value.getAnimatedFraction, colorFrom, colorTo)
          view.setBackgroundColor(color)
        }
      })
      valueAnimator.addListener(new AnimatorListenerAdapter {
        override def onAnimationEnd(animation: Animator): Unit = {
          super.onAnimationEnd(animation)
          view.setLayerType(View.LAYER_TYPE_NONE, null)
          animPromise.success()
        }
      })
      valueAnimator.start()
      animPromise.future
  }

  private[this] def extractDelay(view: View): Int = Option(view.getTag(R.id.fab_menu_position)) match {
    case Some(position) => Try(defaultDelay * Int.unbox(position)) match {
      case Success(delay) => delay
      case Failure(_) => noDelay
    }
    case _ => noDelay
  }

}