package com.x.nocrap.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.x.nocrap.R
import com.x.nocrap.application.AppController
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln

class Utils {
    companion object {
        const val PERMISSION_REQUEST = 101
        const val ADMOB_TEST_DEVICE = "85B1A5A04C8259C15AF23AA4C59BF378"
        var sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")
        fun dp2px(context: Context, dpVal: Int): Int {
            val scale = context.resources.displayMetrics.density
            return (dpVal * scale + 0.5f).toInt()
        }

        fun sp2px(context: Context, spVal: Int): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (spVal * fontScale + 0.5f).toInt()
        }

        fun getCacheDirectory(): File {
            val f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + AppController.getInstance()
                    .getString(R.string.app_name) + "/.cache"
            )
            f.mkdirs()
            return f
        }

        /**
         * return bitmap from vector drawables
         * ((BitmapDrawable) AppCompatResources.getDrawable(getTarget().getContext(), R.drawable.ic_thin_arrowheads_pointing_down)).getBitmap()
         */
        @JvmStatic
        fun getBitmapFromVectorDrawable(
            context: Context,
            drawableId: Int
        ): Bitmap {
            var drawable = ContextCompat.getDrawable(context!!, drawableId)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable!!).mutate()
            }
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }


        fun getFileName(ext: String): String {
            return sdf.format(Date().time) + ext
        }


        fun getCache2Directory(): File {
            val f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + AppController.getInstance()
                    .getString(R.string.app_name) + "/.data"
            )
            f.mkdirs()
            return f
        }

        public fun getNumberQuantifier(count: Int): String {
            if (count < 1000) return "" + count
            val exp =
                (ln(count.toDouble()) / ln(1000.0)).toInt()
            val format = DecimalFormat("0.#")
            val value =
                format.format(count / Math.pow(1000.0, exp.toDouble()))
            return String.format("%s%c", value, "kMBTPE"[exp - 1])
        }

        fun getEmotion(emotion: Int): Int {
            when (emotion) {
                0 -> {
                    return R.drawable.ic_wow
                }
                1 -> {
                    return R.drawable.ic_care
                }
                2 -> {
                    return R.drawable.ic_haha
                }
            }
            return R.drawable.ic_care
        }

        fun requestFullScreenIfLandscape(activity: Activity) {
            //if (activity.getResources().getBoolean(R.bool.landscape)) {
            activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
            //}
        }

        fun shareApplication(activity: Activity) {
            val shareIntent = Intent()
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                AppController.getInstance().getString(R.string.app_name)
            )
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                AppController.getInstance().getString(R.string.invite_message)
            )
            shareIntent.action = Intent.ACTION_SEND
            if (shareIntent.resolveActivityInfo(
                    AppController.getInstance().packageManager,
                    0
                ) != null
            ) activity.startActivity(Intent.createChooser(shareIntent, "Share this app"))
        }

        fun launchMarket() {
            val uri = Uri.parse(
                "market://details?id=" + AppController.getInstance().packageName
            )
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            try {
                AppController.getInstance().startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                AppController.getInstance().startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "http://play.google.com/store/apps/details?id=" + AppController.getInstance()
                                .packageName
                        )
                    )
                )
                //   Toast.makeText(ctx, "couldn't launch the market", Toast.LENGTH_LONG).show();
            }
        }

        /* Create the NotificationChannel, but only on API 26+ because
    the NotificationChannel class is new and not in the support library
   */
        fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence =
                    AppController.getInstance().getString(R.string.notification_channel)
                val att = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val description =
                    AppController.getInstance().getString(R.string.notification_channel_msg)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel("1", name, importance)
                val attributes =
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager =
                    AppController.getInstance().getSystemService(
                        NotificationManager::class.java
                    )
                notificationManager.createNotificationChannel(channel)

                //Silent notification channel
                val NOTIFICATION_CHANNEL_ID = "2"
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    AppController.getInstance().getString(R.string.silent_notification),
                    NotificationManager.IMPORTANCE_LOW
                )
                //Configure the notification channel, NO SOUND
                notificationChannel.description =
                    AppController.getInstance().getString(R.string.silent_notification_msg)
                notificationChannel.setSound(null, null)
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        /**
         * Method to perform Permission model request response operations for the app with
         * fallback functionality [Utils.openSettingApp]
         *
         * @param activity
         * @param PERMISSION_REQUEST_CODE
         * @param fragment
         * @return
         */
        fun checkPermissions(
            activity: Activity,
            PERMISSION_REQUEST_CODE: Int,
            permissions: Array<String>,
            fragment: Fragment?,
            msgStringId: Int
        ): Utils.PermissionStatus {
            val unGrantedPermissions =
                ArrayList<String>()
            var shouldShowRequestPermissionRationale = false
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    unGrantedPermissions.add(permissions[i])
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            permissions[i]
                        )
                    ) {
                        shouldShowRequestPermissionRationale = true
                    }
                }
            }
            return if (unGrantedPermissions.size > 0) {
                if (shouldShowRequestPermissionRationale) {
                    Toast.makeText(activity, msgStringId, Toast.LENGTH_LONG).show()
                    Utils.openSettingApp(activity)
                    return Utils.PermissionStatus.ERROR
                }
                if (fragment == null) {
                    ActivityCompat.requestPermissions(
                        activity,
                        permissions, PERMISSION_REQUEST_CODE
                    )
                    return Utils.PermissionStatus.REQUESTED
                } else {
                    fragment.requestPermissions(permissions, PERMISSION_REQUEST_CODE)
                    return Utils.PermissionStatus.REQUESTED
                }
            } else {
                return Utils.PermissionStatus.SUCCESS
            }
        }


        /**
         * Method to open App's Settings info screen to manually revoke permissions
         * its a fallback for permission model
         *
         * @param ctx
         */
        private fun openSettingApp(ctx: Context) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse(
                "package:" + AppController.getInstance().packageName
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            ctx.startActivity(intent)
        }

        /***
         * building ads
         *
         * **/
        fun buildRewardedAd(activity: Activity): RewardedAd? {
            if (!AppController.getInstance().enableAd) {
                return null
            }
            val rewardedAd =
                RewardedAd(activity, activity.getString(R.string.ADMOB_APP_REWARDED_ID))

            val adLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    // Ad successfully loaded.x


                }

                override fun onRewardedAdFailedToLoad(errorCode: Int) {
                    // Ad failed to load.
                }
            }

            rewardedAd.loadAd(
                AdRequest.Builder().build(),
                adLoadCallback
            )
            return rewardedAd
        }

        fun adConfigure(): RequestConfiguration {
            val testDevices: MutableList<String> =
                ArrayList()
            testDevices.add(Utils.ADMOB_TEST_DEVICE)
            return RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
        }


        fun buildInterstitialAd(activity: Activity): InterstitialAd? {
            if (!AppController.getInstance().enableAd) {
                return null
            }
            val interstitialAd =
                InterstitialAd(activity)
            interstitialAd.adUnitId = activity.getString(R.string.ADMOB_APP_INTERSTITIAL_ID)


            interstitialAd.loadAd(
                AdRequest.Builder().build()
            )
            interstitialAd.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
//                    interstitialAd.show()
                }
            }

            return interstitialAd
        }


    }

    enum class PermissionStatus {
        SUCCESS, ERROR, REQUESTED
    }
}