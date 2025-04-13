package com.example.compsci399testproject.utils

import android.content.Context
import androidx.compose.animation.core.Animatable
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSheetsService(private val context: Context) {

    /*

    REPLACE API KEY.

    IT IS JUST A JSON FILE AND WILL BE LOCATED IN THE ASSETS FOLDER UNDER MAIN.

    */
    private val apiKey = "proj-456408-1edd7ee4075c.json"

    private val sheetId = "1fc01HG-saq2C-m_YymefrvSni_gGj2ann1nBb8x6hzk";
    private val sheetRange = "SheetA1"

    private val stream = context.assets.open(apiKey)
    private val credentials = GoogleCredentials.fromStream(stream)
        .createScoped(listOf(SheetsScopes.SPREADSHEETS))

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val requestInitializer = HttpCredentialsAdapter(credentials)

    private val sheetService = Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
        .setApplicationName("WiFinder")
        .build()

    fun storePositionInformation(positionInfo: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = writeRow(listOf(positionInfo))
            print(res)
        }
    }

    var successOrFail: String? = null

    private fun writeRow(rowValues: List<List<String>>): Any {

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

           successOrFail = "Success"


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
            successOrFail = "Fail"

            print(e.details)
        }

        print(result)
    }
}