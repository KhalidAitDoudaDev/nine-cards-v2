package com.fortysevendeg.ninecardslauncher.app.ui.commons

import android.app.Activity
import android.content.{ComponentName, Intent}
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import com.fortysevendeg.ninecardslauncher.process.collection.models.NineCardIntent
import com.fortysevendeg.ninecardslauncher.process.collection.models.NineCardsIntentExtras._
import com.fortysevendeg.ninecardslauncher.process.device.models.Contact
import com.fortysevendeg.ninecardslauncher2.R
import macroid.ActivityContextWrapper

import scala.util.Try

trait LauncherExecutor {

  val typeEmail = "message/rfc822"

  val titleDialogEmail = "Send Email"

  def execute(intent: NineCardIntent)(implicit activityContext: ActivityContextWrapper) = {
    intent.getAction match {
      case `openApp` =>
        (for {
          newIntent <- createIntentForApp(intent)
          activity <- activityContext.original.get
        } yield {
            tryOrNineIntent(activity, newIntent, intent)
          }) getOrElse tryLaunchPackage(intent)
      case `openRecommendedApp` => goToGooglePlay(intent)
      case `openSms` =>
        (for {
          phone <- intent.extractPhone()
          activity <- activityContext.original.get
        } yield {
            val newIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null))
            tryOrError(activity, newIntent)
          }) getOrElse showError
      case `openPhone` =>
        (for {
          phone <- intent.extractPhone()
          activity <- activityContext.original.get
        } yield {
            val newIntent = new Intent(Intent.ACTION_CALL) // TODO Preference for select dial new Intent(Intent.ACTION_DIAL)
            newIntent.setData(Uri.parse(s"tel:$phone"))
            tryOrError(activity, newIntent)
          }) getOrElse showError
      case `openEmail` =>
        (for {
          email <- intent.extractEmail()
          activity <- activityContext.original.get
        } yield {
            val newIntent = new Intent(Intent.ACTION_SEND)
            newIntent.setType(typeEmail)
            newIntent.putExtra(Intent.EXTRA_EMAIL, Array(email))
            tryOrError(activity, Intent.createChooser(newIntent, titleDialogEmail))
          }) getOrElse showError
      case _ => activityContext.original.get map (tryOrError(_, intent)) getOrElse showError
    }
  }

  def execute(contact: Contact)(implicit activityContext: ActivityContextWrapper) = {
    val contactUri = ContactsContract.Contacts.CONTENT_LOOKUP_URI
      .buildUpon()
      .appendPath(contact.lookupKey)
      .build()
    val intent = new Intent(Intent.ACTION_VIEW, contactUri)
    activityContext.original.get match {
      case Some(a) => tryOrError(a, intent)
      case _ => showError
    }
  }

  def launchDial(phoneNumber: Option[String])(implicit activityContext: ActivityContextWrapper) = {
    val intent = new Intent(Intent.ACTION_DIAL)
    phoneNumber foreach (number => intent.setData(Uri.parse(s"tel:$number")))
    activityContext.original.get match {
      case Some(a) => tryOrError(a, intent)
      case _ => showError
    }
  }

  def launchApp(packageName: String)(implicit activityContext: ActivityContextWrapper) =
    (for {
      activity <- activityContext.original.get
      intent <- Option(activity.getPackageManager.getLaunchIntentForPackage(packageName))
    } yield tryOrError(activity, intent)) getOrElse showError

  private[this] def tryOrNineIntent(activity: Activity, newIntent: Intent, nineIntent: NineCardIntent)
    (implicit activityContext: ActivityContextWrapper) =
    if (Try(activity.startActivity(newIntent)).isFailure) tryLaunchPackage(nineIntent)

  private[this] def tryOrError(activity: Activity, newIntent: Intent)
    (implicit activityContext: ActivityContextWrapper) =
    if (Try(activity.startActivity(newIntent)).isFailure) showError

  private[this] def tryOrGooglePlay(activity: Activity, newIntent: Intent, nineIntent: NineCardIntent)
    (implicit activityContext: ActivityContextWrapper) =
    if (Try(activity.startActivity(newIntent)).isFailure) goToGooglePlay(nineIntent)

  private[this] def createIntentForApp(intent: NineCardIntent): Option[Intent] = for {
    packageName <- intent.extractPackageName()
    className <- intent.extractClassName()
  } yield {
      val intent = new Intent(Intent.ACTION_MAIN)
      intent.addCategory(Intent.CATEGORY_LAUNCHER)
      intent.setComponent(new ComponentName(packageName, className))
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
      intent
    }

  private[this] def tryLaunchPackage(intent: NineCardIntent)(implicit activityContext: ActivityContextWrapper) =
    (for {
      packageName <- intent.extractPackageName()
      activity <- activityContext.original.get
    } yield {
        val newIntent = activityContext.application.getPackageManager.getLaunchIntentForPackage(packageName)
        tryOrGooglePlay(activity, newIntent, intent)
      }) getOrElse showError

  private[this] def goToGooglePlay(intent: NineCardIntent)(implicit activityContext: ActivityContextWrapper) =
    (for {
      packageName <- intent.extractPackageName()
      activity <- activityContext.original.get
    } yield {
        val newIntent = new Intent(Intent.ACTION_VIEW,
          Uri.parse(activityContext.application.getString(R.string.google_play_url, packageName)))
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        tryOrError(activity, newIntent)
      }) getOrElse showError

  private[this] def showError(implicit activityContext: ActivityContextWrapper) =
    Toast.makeText(activityContext.application, R.string.contactUsError, Toast.LENGTH_SHORT).show()

}