package com.commodots.umcaaconnecta.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.commodots.umcaaconnecta.R


class PrivacyAndPolicyActivity: AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 451

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_and_policy)

        findViewById<Button>(R.id.btn_agree).setOnClickListener {

            showContactsConfirmationDialog()

        }


    }

    private fun showContactsConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Agreement")
        dialog.setCancelable(false)


        val view = LayoutInflater.from(this).inflate(R.layout.privacy_policy_dialog, null, false)
        dialog.setView(view)

        val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)

        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text = "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"


        getHtml4(tv)

        dialog.setNegativeButton("DECLINE", null)

        dialog.setPositiveButton("AGREE") { dialog, which ->
            val editor = getSharedPreferences("agreePrivacy", MODE_PRIVATE).edit()
            editor.putString("isAllow", "allow")
            editor.apply()

            startLoginActivity()

        }

        val mDialog = dialog.show()
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->

            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked

        }

    }


    private fun getHtml4(textView: TextView){

        val html = resources.getString(R.string.privacy_policy_html)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        } else {
            textView.setText(Html.fromHtml(html))
        }

    }

    private fun startLoginActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

}
