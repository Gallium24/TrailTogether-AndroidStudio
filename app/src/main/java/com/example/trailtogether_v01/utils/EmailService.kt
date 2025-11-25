package com.example.trailtogether_v01.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailService {
    private const val SENDER_EMAIL = "jeremy.zedek@gmail.com"
    private const val SENDER_PASSWORD = "gitsfbivobeypdjj"

    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"

    suspend fun sendEmergencyEmail(recipientEmail: String, userName: String, trailName: String, date: String) {
        withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                }

                val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                })

                MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "URGENCE : Pas de nouvelles de $userName (TrailTogether)"
                    setText("""
                        Ceci est un message automatique de TrailTogether.
                        
                        L'utilisateur $userName avait prévu une randonnée "$trailName" le $date.
                        
                        Il n'a pas signalé son retour 24 heures après le début prévu.
                        Veuillez essayer de le contacter.
                    """.trimIndent())

                    Transport.send(this)
                }
                Log.d("EmailService", "E-mail envoyé via $SMTP_HOST")
            } catch (e: Exception) {
                Log.e("EmailService", "Erreur envoi email", e)
            }
        }
    }
}