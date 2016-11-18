package cards.nine.app.ui.collections.jobs

import cards.nine.app.commons.{AppNineCardsIntentConversions, Conversions}
import cards.nine.app.ui.collections.jobs.uiactions.{ScrollType, ToolbarUiActions}
import cards.nine.app.ui.commons.Jobs
import cards.nine.commons.services.TaskService.TaskService
import macroid.ActivityContextWrapper

class ToolbarJobs(actions: ToolbarUiActions)(implicit activityContextWrapper: ActivityContextWrapper)
  extends Jobs
    with Conversions
    with AppNineCardsIntentConversions { self =>

  def pullToClose(scroll: Int, scrollType: ScrollType, close: Boolean): TaskService[Unit] =
    actions.pullCloseScrollY(scroll, scrollType, close)

}
