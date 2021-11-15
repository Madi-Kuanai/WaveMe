package com.mzmm.wave_me.sign_in_out

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.mzmm.wave_me.MainActivity
import com.mzmm.wave_me.R
import com.mzmm.wave_me.databinding.ActivitySignUpBinding
import java.lang.NullPointerException
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignUp : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var auth: FirebaseAuth
    lateinit var launcher: ActivityResultLauncher<Intent>
    val PASSWORD_REGEX_PATTERN = """^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$"""
    lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Dialog(this)
        dialog.setContentView(R.layout.wait_dialog)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        auth = Firebase.auth
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

        binding.signUp.setOnClickListener(View.OnClickListener {
            if (checkEdits()) {
                dialog.show()
                auth.createUserWithEmailAndPassword(
                    binding.emailField.text.toString(),
                    binding.passwordField.text.toString()
                ).addOnCompleteListener(OnCompleteListener {
                    if (it.isSuccessful) {
                        val user: User =
                            User(
                                binding.name.text.toString(),
                                binding.emailField.text.toString()
                            )
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(user)
                            .addOnCompleteListener(
                                OnCompleteListener { res ->
                                    if (res.isSuccessful) {
                                        Log.d("MyLog", "SignUp Success")
                                        isUserExist()
                                    } else {
                                        Log.d("MyLog", "Sign Up Failed")
                                    }
                                })
                    } else {
                        if (it.exception?.message.toString() == "The email address is already in use by another account.") {
                            binding.emailField.error = resources.getString(R.string.email_error)
                        }
                        Log.d("MyLog", it.exception?.message.toString())
                        dialog.hide()
                    }
                })
            }

        })
        binding.toSignIn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            finish()
        })
        binding.google.setOnClickListener(View.OnClickListener {
            try {
                googleAuth()
            } catch (e: NullPointerException) {
                Log.d("MyLog", e.message.toString())
            }

        })
    }

    fun googleAuth() {
        val client = getClient()
        launcher.launch(client.signInIntent)
    }

    private fun checkEdits(): Boolean {
        var boolean = true
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailField.text.toString()).matches()) {
            binding.emailField.error = "Error"
            boolean = false
        }
        if (binding.name.text.isEmpty()) {
            binding.name.error = "Empty"
            boolean = false
        }
        if (!isValidPassword(binding.passwordField.text.toString())) {
            binding.passwordField.error = "Essy password"
            boolean = false
        }
        if (binding.confirmPassword.text.toString() != binding.passwordField.text.toString()) {
            binding.confirmPassword.error = "Error"
            boolean = false
        }
        return boolean
    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern =
            Pattern.compile(PASSWORD_REGEX_PATTERN)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    private fun isUserExist() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
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

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("23133608412-hsgbsemiil3tfpf1vc8p94c5l0mcb0p1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }
}