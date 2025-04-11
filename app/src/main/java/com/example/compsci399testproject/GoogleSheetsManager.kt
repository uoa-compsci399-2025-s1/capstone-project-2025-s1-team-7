package com.example.compsci399testproject

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.Credential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.*


/*

    The JSON Request Body Looks Like This

    This appends data to the next empty row of the sheet

    "values" contains the data appended to sheet

    {
      "values": [
        [
          "longitude",
          "latitude",
          "floor"
        ]
      ]
    }

*/


// Using OAuth

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signIn(context: Context, credentialManager: CredentialManager, request: GetCredentialRequest): GoogleIdTokenCredential? {
    return try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )
        print("""
            
            
                CRED              ✅
                              ✅
                           ✅
                        ✅
                      ✅
           ✅       ✅
              ✅  ✅
                ✅


        """)
        handleSignIn(result)
    } catch (e: Error) {
        print("""


         ❌                      ❌
            ❌                ❌
               ❌          ❌
               
                   NO CRED 
               
               ❌          ❌
            ❌                ❌
         ❌                      ❌


        """)
        print(e)
        null
    }
}

fun handleSignIn(result: GetCredentialResponse): GoogleIdTokenCredential? {
    return result.credential as GoogleIdTokenCredential?
}

suspend fun writeRow(sheetService: Sheets, rowValues: List<List<String>>): Any {

    val sheetId = "1fc01HG-saq2C-m_YymefrvSni_gGj2ann1nBb8x6hzk";
    val sheetRange = "SheetA1"

    val requestBody = ValueRange().setValues(rowValues)

    var result: Any

    return try {
        result = sheetService.spreadsheets().values()
            .append(sheetId, sheetRange, requestBody)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .setIncludeValuesInResponse(true)
            .execute()

        print("""


                                      ✅
                                  ✅
                               ✅
                            ✅
                          ✅
               ✅       ✅
                  ✅  ✅
                    ✅


        """)

    } catch (e: GoogleJsonResponseException) {
       result = e.details


        print("""


             ❌                      ❌
                ❌                ❌
                   ❌          ❌
                      ❌    ❌
                         ❌
                      ❌    ❌
                   ❌          ❌
                ❌                ❌
             ❌                      ❌


        """)

        print(e)
    }

    return result
}

fun storePositionInformation(context: Context, account: GoogleIdTokenCredential?, positionInfo: List<String>) {

    print("""
       
            Store
      
            $account
        
    """)

    if (account == null) {
        println("❌ No account provided.")
        return
    }

    val credential = GoogleAccountCredential.usingOAuth2(context, sc)

//    val credential = GoogleCredentials.create(AccessToken(account.idToken, null))
//        .createScoped(listOf(SheetsScopes.SPREADSHEETS))

    val jsonFactory = GsonFactory.getDefaultInstance()
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val requestInitializer = HttpCredentialsAdapter(credential)

    val sheetService = Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
        .setApplicationName("WiFinder")
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        val res = writeRow(sheetService, listOf(positionInfo))
        print(res)

//        withContext(Dispatchers.Main) {
//        }
    }
}



// Using Service Worker

//class GoogleSheetsManager(private val context: Context) {
//
//    private val sheetId = "1fc01HG-saq2C-m_YymefrvSni_gGj2ann1nBb8x6hzk";
//    private val sheetRange = "SheetA1"
//
////    private val stream = context.assets.open(key)
////    private val credentials = GoogleCredentials.fromStream(stream)
////        .createScoped(listOf(SheetsScopes.SPREADSHEETS))
////
////    private val jsonFactory = GsonFactory.getDefaultInstance()
////    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
////    private val requestInitializer = HttpCredentialsAdapter(credentials)
//
//
//    private val sheetService = Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
//        .setApplicationName("WiFinder")
//        .build()
//
//
//    suspend fun writeRow(rowValues: List<List<String>>): Any {
//
//        val requestBody = ValueRange().setValues(rowValues)
//
//        var result: Any
//
//        return try {
//            result = sheetService.spreadsheets().values()
//                .append(sheetId, sheetRange, requestBody)
//                .setValueInputOption("USER_ENTERED")
//                .setInsertDataOption("INSERT_ROWS")
//                .setIncludeValuesInResponse(true)
//                .execute()
//
//            print("""
//
//
//                                          ✅
//                                      ✅
//                                   ✅
//                                ✅
//                              ✅
//                   ✅       ✅
//                      ✅  ✅
//                        ✅
//
//
//            """)
//
//        } catch (e: GoogleJsonResponseException) {
//           result = e.details
//
//
//            print("""
//
//
//                 ❌                      ❌
//                    ❌                ❌
//                       ❌          ❌
//                          ❌    ❌
//                             ❌
//                          ❌    ❌
//                       ❌          ❌
//                    ❌                ❌
//                 ❌                      ❌
//
//
//            """)
//
//            print(e)
//        }
//
//        return result
//    }
//
//    fun storePositionInformation(positionInfo: List<String>) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val res = writeRow(listOf(positionInfo))
//            print(res)
//
////            withContext(Dispatchers.Main) {
////            }
//        }
//    }
//}
