package com.margelo.nitro.incomingcall

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IncomingCallActivity : AppCompatActivity() {

    private var uuid: String = ""
    private var callerName: String = "Unknown"
    private var callType: String = "audio"
    private var timeout: Long = 30000L
    private var countDownTimer: CountDownTimer? = null
    private var timerView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockScreenFlags()

        uuid = intent.getStringExtra("uuid") ?: ""
        callerName = intent.getStringExtra("callerName") ?: "Unknown"
        callType = intent.getStringExtra("callType") ?: "audio"
        timeout = intent.getLongExtra("timeout", 30000L)
        val backgroundColor = intent.getStringExtra("backgroundColor")

        val bgColor = try {
            if (backgroundColor != null) Color.parseColor(backgroundColor)
            else Color.parseColor("#1A1A2E")
        } catch (_: Exception) {
            Color.parseColor("#1A1A2E")
        }

        buildUI(bgColor)
        startCountdown()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lock screen / wake flags
    // ──────────────────────────────────────────────────────────────────────────

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UI (built programmatically – no XML resources needed from the library)
    // ──────────────────────────────────────────────────────────────────────────

    private fun buildUI(bgColor: Int) {
        val root = RelativeLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ── Avatar (circle with initials) ──────────────────────────────────
        val initials = callerName
            .trim()
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it[0].uppercaseChar().toString() }
            .ifEmpty { "?" }

        val avatarSize = dp(100)
        val avatar = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(avatarSize, avatarSize).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                topMargin = dp(110)
            }
            text = initials
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 38f)
            gravity = Gravity.CENTER
            background = circle(Color.parseColor("#3D5AFE"))
        }
        root.addView(avatar)

        // ── "Incoming call" subtitle ────────────────────────────────────────
        val subtitle = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.BELOW, avatar.id)
                topMargin = dp(14)
            }
            text = "Incoming ${callType} call"
            setTextColor(Color.parseColor("#AAAAAA"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        root.addView(subtitle)

        // ── Caller name ─────────────────────────────────────────────────────
        val nameView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.BELOW, subtitle.id)
                topMargin = dp(6)
            }
            text = callerName
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
        }
        root.addView(nameView)

        // ── Countdown timer ─────────────────────────────────────────────────
        timerView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.BELOW, nameView.id)
                topMargin = dp(10)
            }
            setTextColor(Color.parseColor("#AAAAAA"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        root.addView(timerView)

        // ── Bottom action buttons ───────────────────────────────────────────
        val buttonsRow = LinearLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                bottomMargin = dp(80)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        // Decline button group
        val declineGroup = buttonGroup(
            label = "Decline",
            emoji = "✕",
            color = Color.parseColor("#E53935"),
            onClick = { onRejected() }
        )

        // Accept button group
        val acceptGroup = buttonGroup(
            label = "Accept",
            emoji = "✆",
            color = Color.parseColor("#43A047"),
            onClick = { onAnswered() }
        )
        (acceptGroup.layoutParams as LinearLayout.LayoutParams).leftMargin = dp(80)

        buttonsRow.addView(declineGroup)
        buttonsRow.addView(acceptGroup)
        root.addView(buttonsRow)

        setContentView(root)
    }

    /** Creates a vertical group: circle button + label */
    private fun buttonGroup(
        label: String,
        emoji: String,
        color: Int,
        onClick: () -> Unit
    ): LinearLayout {
        val group = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val btnSize = dp(72)
        val btn = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
            text = emoji
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            gravity = Gravity.CENTER
            background = circle(color)
            setOnClickListener { onClick() }
        }
        group.addView(btn)

        val lbl = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
            text = label
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
        }
        group.addView(lbl)
        return group
    }

    private fun circle(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    // ──────────────────────────────────────────────────────────────────────────
    // Timer
    // ──────────────────────────────────────────────────────────────────────────

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(timeout, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerView?.text = "Auto-declining in ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                onTimeout()
            }
        }.start()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Call actions
    // ──────────────────────────────────────────────────────────────────────────

    private fun onAnswered() {
        countDownTimer?.cancel()
        broadcast(IncomingCallModule.ACTION_ANSWER)
        cleanup()
    }

    private fun onRejected() {
        countDownTimer?.cancel()
        broadcast(IncomingCallModule.ACTION_REJECT)
        cleanup()
    }

    private fun onTimeout() {
        broadcast(IncomingCallModule.ACTION_TIMEOUT)
        cleanup()
    }

    private fun broadcast(action: String) {
        val intent = Intent(action).apply {
            putExtra("uuid", uuid)
            `package` = packageName
        }
        sendBroadcast(intent)
    }

    private fun cleanup() {
        stopService(Intent(this, IncomingCallService::class.java))
        finish()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from dismissing the incoming call screen
    }
}
