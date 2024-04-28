package com.cad.proyectofinaletps1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class Login : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnIniciarSesion)
        val edtCorreo = findViewById<EditText>(R.id.edtCorreo)
        val edtPass = findViewById<EditText>(R.id.edtPass)
        val  lblCorreo = findViewById<TextView>(R.id.lblCorreo)
        val lblpass = findViewById<TextView>(R.id.lblPass)
        val imvGoogle = findViewById<ImageView>(R.id.imvGoogle)

        edtCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    lblCorreo.visibility = View.INVISIBLE
                } else {
                    lblCorreo.visibility = View.VISIBLE
                }
            }
        })

        edtPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    lblpass.visibility = View.INVISIBLE
                } else {
                    lblpass.visibility = View.VISIBLE
                }
            }
        })

        btnLogin.setOnClickListener {
            if (edtCorreo.text.isNotEmpty() && edtPass.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(edtCorreo.text.toString(),
                    edtPass.text.toString()).addOnCompleteListener{
                        if (it.isSuccessful){
                            ShowHome(it.result?.user?.email ?:"", ProviderType.BASIC)
                        }else{
                            ShowAlert()
                        }
                }
            }
        }

        imvGoogle.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this,googleConf)
            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
        }

    }

    private fun ShowAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun ShowHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this,MainActivity::class.java).apply {
            putExtra("Mail",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if (it.isSuccessful){
                            ShowHome(account.email?: "", ProviderType.GOOGLE)
                        }else{
                            ShowAlert()
                        }
                    }
                }
            } catch (e: ApiException){
                ShowAlert();
            }
        }
    }

}