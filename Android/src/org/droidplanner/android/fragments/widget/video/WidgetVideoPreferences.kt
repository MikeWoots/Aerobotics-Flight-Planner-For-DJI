package org.droidplanner.android.fragments.widget.video

import android.app.DialogFragment
import android.os.Bundle
import android.support.annotation.IntDef
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import org.droidplanner.android.R
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs
import timber.log.Timber

/**
 * Created by Fredia Huya-Kouadio on 10/18/15.
 */
class WidgetVideoPreferences : DialogFragment() {

    companion object {
        @JvmStatic const val SOLO_VIDEO_TYPE = 0
        @JvmStatic const val CUSTOM_VIDEO_TYPE = 1
    }

    @IntDef(SOLO_VIDEO_TYPE.toLong(), CUSTOM_VIDEO_TYPE.toLong())
    @Retention(AnnotationRetention.SOURCE)
    annotation class VideoType

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_video_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = activity.applicationContext
        val appPrefs = DroidPlannerPrefs(context)

        val udpPortView = view.findViewById(R.id.custom_video_provider_udp_port) as EditText?

        val currentUdpPort = appPrefs.customVideoUdpPort
        val currentEntry = if(currentUdpPort == -1) "" else "$currentUdpPort"
        udpPortView?.setText(currentEntry)

        udpPortView?.setOnEditorActionListener { textView, actionId, keyEvent ->
            when(actionId){
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL -> {
                    val entry = textView.text
                    try {
                        if (!TextUtils.isEmpty(entry)) {
                            val udpPort = entry.toString().toInt();
                            appPrefs.customVideoUdpPort = udpPort
                        }
                    }catch(e: NumberFormatException){
                        Timber.e(e, "Invalid udp port value: %s", entry)
                        Toast.makeText(context, "Invalid udp port!", Toast.LENGTH_LONG).show()

                        val currentUdpPort = appPrefs.customVideoUdpPort
                        val currentEntry = if(currentUdpPort == -1) "" else "$currentUdpPort"
                        udpPortView.setText(currentEntry)
                    }
                }
            }
            true
        }

        val radioGroup = view.findViewById(R.id.video_widget_pref) as RadioGroup?

        val currentVideoType = appPrefs.videoWidgetType
        when(currentVideoType){
            SOLO_VIDEO_TYPE -> radioGroup?.check(R.id.solo_video_stream_check)
            CUSTOM_VIDEO_TYPE -> radioGroup?.check(R.id.custom_video_stream_check)
        }

        radioGroup?.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.solo_video_stream_check -> {
                    udpPortView?.isEnabled = false
                    appPrefs.videoWidgetType = SOLO_VIDEO_TYPE
                }

                R.id.custom_video_stream_check -> {
                    udpPortView?.isEnabled = true
                    appPrefs.videoWidgetType = CUSTOM_VIDEO_TYPE
                }
            }
        }
    }
}