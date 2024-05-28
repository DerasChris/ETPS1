package com.cad.proyectofinaletps1

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Login : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.btnIniciarSesion)
        val btnSignUp = findViewById<Button>(R.id.btnCrearCuenta)
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
            // To-Do: Remove this and move it to the nav bar
            /*val presupuestosIntent = Intent(this,PresupuestosUsuario::class.java)
            startActivity(presupuestosIntent)*/

            if (edtCorreo.text.isNotEmpty() && edtPass.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(edtCorreo.text.toString(),
                    edtPass.text.toString()).addOnCompleteListener{
                        if (it.isSuccessful){
                            ShowHome(it.result?.user?.email ?:"", ProviderType.BASIC)
                        }else{
                            ShowAlert("Se ha producido un error al autenticar al usuario")
                        }
                }
            }
        }

        btnSignUp.setOnClickListener {
            if (edtCorreo.text.isNotEmpty() && edtPass.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtCorreo.text.toString(),edtPass.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            ShowOerlay("Cuenta creada con éxito")
                        } else {
                            ShowAlert("Se ha producido un error al crear al usuario")
                        }
                    }
            }
        }

        imvGoogle.setOnClickListener {
            // Configurar opciones de inicio de sesión de Google

            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            // Crear cliente de inicio de sesión de Google
            val googleClient = GoogleSignIn.getClient(this, googleConf)


            googleClient.signOut()

            // Iniciar sesión de Google
            val signInIntent = googleClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)



        }

    }

    private fun ShowAlert(message:String) {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_fail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val mensaje : TextView = dialog.findViewById(R.id.txtMessage)

        mensaje.text = message

        dialog.show()
    }

    private fun ShowOerlay(message:String){
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_success)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val mensaje : TextView = dialog.findViewById(R.id.txtMessage)

        mensaje.text = message

        dialog.show()

    }

    private fun ShowHome(email: String, provider: ProviderType,user:String, urlPhoto: String){
        val homeIntent = Intent(this,navegacion::class.java).apply {
            putExtra("Mail",email)
            putExtra("User",user)
            putExtra("provider",provider.name)
            putExtra("url",urlPhoto)
        }
        startActivity(homeIntent)
    }

    private fun ShowHome(email: String, provider: ProviderType,){
        val homeIntent = Intent(this,navegacion::class.java).apply {
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
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            val displayName = user?.displayName
                            val email = user?.email ?: ""
                            val userId = user?.uid ?: ""
                            val profile = user?.photoUrl ?: ""

                            Log.d(TAG,"LA URL DE LA FOTO ES: $profile")

                            // Verificar si el usuario ya está registrado
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("usuarios")

                            usersRef.child("usuario_$userId").addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        // El usuario ya está registrado, no es necesario guardar los datos nuevamente
                                        // Guardar el UUID en SharedPreferences
                                        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.putString("userUUID", "usuario_$userId")
                                        editor.apply()
                                        ShowHome("$email", ProviderType.GOOGLE,"$displayName",profile.toString())

                                    } else {
                                        // El usuario no está registrado, guardar sus datos en la base de datos
                                        val userData = HashMap<String, Any>()
                                        userData["nombre"] = displayName ?: ""
                                        userData["correo"] = email
                                        userData["uuid"] = userId

                                        usersRef.child("usuario_$userId").setValue(userData)

                                        // Guardar el UUID en SharedPreferences
                                        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.putString("userUUID", userId)
                                        editor.apply()

                                        ShowHome("$email", ProviderType.GOOGLE)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Manejar el error de la consulta
                                    ShowAlert("Error al verificar el usuario en la base de datos")
                                }
                            })
                        } else {
                            ShowAlert("Sesión no iniciada")
                        }
                    }
                }
            } catch (e: ApiException) {
                ShowAlert("No se pudo iniciar sesión")
            }
        }
    }


}