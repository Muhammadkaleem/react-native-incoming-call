package com.margelo.nitro.incomingcall

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.uimanager.ThemedReactContext

/**
 * NitroView for displaying an inline incoming call UI inside a React Native screen
 * (foreground / in-app use). For background / lock-screen display use the
 * IncomingCallModule.display() API which starts the full-screen Activity.
 */
@DoNotStrip
class HybridIncomingCall(private val ctx: ThemedReactContext) : HybridIncomingCallSpec() {

  // ── Root layout ─────────────────────────────────────────────────────────────
  private val rootLayout = RelativeLayout(ctx).apply {
    setBackgroundColor(Color.parseColor("#1A1A2E"))
  }

  private val avatarView = TextView(ctx).apply {
    gravity = Gravity.CENTER
    setTextColor(Color.WHITE)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
    background = GradientDrawable().apply {
      shape = GradientDrawable.OVAL
      setColor(Color.parseColor("#3D5AFE"))
    }
  }

  private val callerNameView = TextView(ctx).apply {
    setTextColor(Color.WHITE)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
    gravity = Gravity.CENTER
  }

  private val subtitleView = TextView(ctx).apply {
    setTextColor(Color.parseColor("#AAAAAA"))
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
    gravity = Gravity.CENTER
    text = "Incoming audio call"
  }

  override val view: View = rootLayout

  init {
    buildLayout()
  }

  private fun buildLayout() {
    val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }

    val avatarSize = dp(72)
    avatarView.apply {
      id = View.generateViewId()
      layoutParams = RelativeLayout.LayoutParams(avatarSize, avatarSize).apply {
        addRule(RelativeLayout.CENTER_HORIZONTAL)
        topMargin = dp(20)
      }
      text = "?"
    }
    rootLayout.addView(avatarView)

    subtitleView.apply {
      id = View.generateViewId()
      layoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        addRule(RelativeLayout.BELOW, avatarView.id)
        topMargin = dp(8)
      }
    }
    rootLayout.addView(subtitleView)

    callerNameView.apply {
      id = View.generateViewId()
      layoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        addRule(RelativeLayout.BELOW, subtitleView.id)
        topMargin = dp(4)
      }
    }
    rootLayout.addView(callerNameView)

    // Answer / Reject buttons row
    val buttonsRow = LinearLayout(ctx).apply {
      id = View.generateViewId()
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER
      layoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        bottomMargin = dp(16)
      }
    }

    val btnSize = dp(52)

    val rejectBtn = TextView(ctx).apply {
      layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
        rightMargin = dp(40)
      }
      text = "✕"
      setTextColor(Color.WHITE)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      gravity = Gravity.CENTER
      background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.parseColor("#E53935"))
      }
      setOnClickListener { rejectCall() }
    }

    val answerBtn = TextView(ctx).apply {
      layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
      text = "✆"
      setTextColor(Color.WHITE)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      gravity = Gravity.CENTER
      background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.parseColor("#43A047"))
      }
      setOnClickListener { answerCall() }
    }

    buttonsRow.addView(rejectBtn)
    buttonsRow.addView(answerBtn)
    rootLayout.addView(buttonsRow)
  }

  // ── Props ────────────────────────────────────────────────────────────────────

  private var _color = "#1A1A2E"
  override var color: String
    get() = _color
    set(value) {
      _color = value
      try { rootLayout.setBackgroundColor(value.toColorInt()) } catch (_: Exception) {}
    }

  private var _callerName: String? = null
  override var callerName: String?
    get() = _callerName
    set(value) {
      _callerName = value
      callerNameView.text = value ?: ""
      updateAvatar()
    }

  private var _avatar: String? = null
  override var avatar: String?
    get() = _avatar
    set(value) {
      _avatar = value
      // Avatar URL loading can be added via an image-loading library;
      // for now we keep the initials fallback.
      updateAvatar()
    }

  private var _callType: String? = "audio"
  override var callType: String?
    get() = _callType
    set(value) {
      _callType = value ?: "audio"
      subtitleView.text = "Incoming ${_callType} call"
    }

  private var _timeout: Double? = 30000.0
  override var timeout: Double?
    get() = _timeout
    set(value) { _timeout = value }

  // ── Methods ──────────────────────────────────────────────────────────────────

  override fun answerCall() {
    val uuid = IncomingCallModule.currentCallUuid ?: ""
    val intent = android.content.Intent(IncomingCallModule.ACTION_ANSWER).apply {
      putExtra("uuid", uuid)
      `package` = ctx.packageName
    }
    ctx.sendBroadcast(intent)
  }

  override fun rejectCall() {
    val uuid = IncomingCallModule.currentCallUuid ?: ""
    val intent = android.content.Intent(IncomingCallModule.ACTION_REJECT).apply {
      putExtra("uuid", uuid)
      `package` = ctx.packageName
    }
    ctx.sendBroadcast(intent)
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private fun updateAvatar() {
    val name = _callerName ?: ""
    val initials = name
      .trim()
      .split(" ")
      .filter { it.isNotEmpty() }
      .take(2)
      .joinToString("") { it[0].uppercaseChar().toString() }
      .ifEmpty { "?" }
    avatarView.text = initials
  }
}
