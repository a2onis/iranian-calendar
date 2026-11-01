package ir.gahshomar.parsi.notify

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

/**
 * نگه‌داریِ پنجرهٔ تاریخ، بی هیچ اعلانی.
 *
 * اندروید خدمت‌های پس‌زمینهٔ بی‌اعلان را پس از چندی می‌خوابانَد؛ اگر پنجرهٔ تاریخ
 * (راهِ «نمایش روی برنامه‌های دیگر») خوابید و سرویس دسترس‌پذیری هم روشن نبود، این
 * کارِ زمان‌بندی‌شده هر پانزده دقیقه آن را دوباره برمی‌خیزاند و ویجت را هم تازه می‌کند.
 */
class OverlayKeepAliveJob : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        runCatching { OverlayService.sync(this) }
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean = false

    companion object {
        private const val JOB_ID = 2585
        private const val INTERVAL_MILLIS = 15 * 60 * 1000L

        fun ensure(context: Context) {
            runCatching {
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
                    ?: return
                if (!OverlayService.canDraw(context) || LockDateOverlay.isEnabled(context)) {
                    scheduler.cancel(JOB_ID)
                    return
                }
                val info = JobInfo.Builder(
                    JOB_ID,
                    ComponentName(context, OverlayKeepAliveJob::class.java)
                )
                    .setPersisted(true)
                    .setPeriodic(INTERVAL_MILLIS)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .build()
                scheduler.schedule(info)
            }
        }
    }
}
