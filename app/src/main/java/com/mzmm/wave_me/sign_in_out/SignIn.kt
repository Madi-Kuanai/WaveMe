package com.mzmm.wave_me.sign_in_out

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mzmm.wave_me.MainActivity
import com.mzmm.wave_me.R
import com.mzmm.wave_me.databinding.ActivitySignInBinding
import java.lang.NullPointerException


class SignIn : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Dialog(this)
        dialog.setContentView(R.layout.wait_dialog)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)



        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(token = account.idToken)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", e.message.toString())
            }

        }

        auth = Firebase.auth
        try {
            binding.signIn.setOnClickListener {
                dialog.show()
                auth.signInWithEmailAndPassword(
                    binding.emailField.text.toString(),
                    binding.passwordField.text.toString()
                ).addOnCompleteListener(OnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("MyLog", "Success")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        if (it.exception?.message.toString() == "The password is invalid or the user does not have a password.") {

                        }
                        Log.d("MyLog", it.exception?.message.toString())
                    }
                })
                dialog.hide()
                Log.d("MyLog", "Clicked")
            }
            binding.google.setOnClickListener {
                googleAuth()
            }
        } catch (e: NullPointerException) {
            Log.d("MyLog", e.message.toString())
        }
        isUserExist()
        binding.toRegistration.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        })
    }

    fun googleAuth() {
        val client = getClient()
        launcher.launch(client.signInIntent)
    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("23133608412-hsgbsemiil3tfpf1vc8p94c5l0mcb0p1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    private fun firebaseAuthWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful) {
                Log.d("MyLog", "firebaseAuthWithGoogle is Successful")
                isUserExist()
            }
        })
    }

    private fun isUserExist() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RESULT_OK && resultCode == 100) {
            googleAuth()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}