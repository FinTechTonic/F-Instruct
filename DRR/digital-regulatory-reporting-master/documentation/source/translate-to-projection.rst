Translate-To-Report-To-Projection
===================

This page illustrates steps to complete an end to end journey from Translate To Report To Projection using the DRR functional model.

Using the DRR Model a user can convert an FpML Recordkeeping message in .xml format, either as a preloaded test pack file, or by uploading a custom file, through the `Translate` service to generate the same information in CDM type `ReportableEvent`.

The resulting type can be downloaded in in .json format, then run through a `Custom Function` to enrich the ingested `ReportableEvent`, create a default `ReportingSide`, and finally combine two objects to create a `TransactionReportInstruction`. This `TransactionReportInstruction` can be used as an input into the `Report` service to generate a transaction report, such as a `ESMAEMIRTransactionReport`.

The output from the `Report` service can be downloaded in .json format, then used as the input for the `Projection` service to transform the transaction report into a ISO 20022 xml format.

The above can be achieved in two ways using the Rosetta UI or via Postman request calls. Detailed steps for both are illustrated below:-

Via Rosetta UI
-----------------

Within Rosetta create a DRR workspace.

**Running Ingestion Service**

* Click on the `Translate` at the bottom of the screen to open the Translate panel you can use upload option to upload your own example or select one from the given example list as shown below `Example 02 Submission 1`.

   .. image:: images/translate-service.png

* Click on the file to run Translate Service, this will create a CDM model from in the ingestion file as shown below.

   .. image:: images/translate-service-response.png

* Under the CDM panel you can see the input data shown with in CDM Model. Click on the download button at the right hand side to download CDM model in Json format. Save this file to use in next steps.

**Running Custom Function Service**

* Click on the `Function` at the bottom of the screen to open the Function panel. Select `Create_TransactionReportInstructionFromInstructionDefault` function from list. You can use `upload` button to upload the file downloaded the above step. `Example 02 Submission 1`.

   .. image:: images/cust-func.png

* This will generate `TransactionReportInstruction` from the ingested `ReportableEvent` .json file. The `TransactionReportInstruction` in shown in the OUTPUT panel with details of both `reportingSide` :- `reportingParty` and `reportingCounterparty`.

   .. image:: images/cust-func-response.png

* Click on the download button at the right hand side to download this TransactionReportInstruction in Json format. Save this file to use in next steps.

**Running Report Service**

* Click on the `Reports` at the bottom of the screen to open the Reports panel. Select a report, for example `ESMA / EMIR Trade`, then click the Upload button to upload the file downloaded the previous step. Select file type `drr.regulation.common.TransactionReportInstruction` and then click upload.

   .. image:: images/reports-service.png

* You will see the uploaded file shown on the screen as below.

   .. image:: images/report-upload.png

* Click on the file to view the Report.

   .. image:: images/reports-service-response.png

**Running Projection Service**

* Click on the `Projection` at the bottom of the screen to open the Projection panel. Select a projection, for example `EsmaEmirTradeReportToIso20022`, then click the `Upload` button to upload the file that was downloaded the previous step and then click upload.

   .. image:: images/projection-service.png

* You will see the uploaded file shown on the screen as below.

   .. image:: images/projection-upload.png

Via Postman
-----------

**Running Ingestion Service**

user can ingest their example file to translate into CDM mode using steps below

* Within Rosetta navigate to `DRR Model` and click on `API Export` at the Bottom of the screen. Click on the `FpML_5_10_RecordKeeping-ingestion-service` from the list of API's on the left. As shown below.

   .. image:: images/api-export-ingestion-service.png

* In the right hand side of the screen you will see the `INGESTION SERVICE` details such `Base URL` and `API Key`.
* Copy the URL and Open Postman create new request. Set request type as `POST` and paste the URL copied in the step above.

   .. image:: images/postman-insgestion-url.png

* Open the `Header` tab under column `Key` type `Authorization` and with in `Value` copy and paste the value from `API Key` from Rosetta Screen as shown in screen shot above.
* Open tab `Body` in Postman. Select option `raw` and type `XML` as shown below.

   .. image:: images/postMmn-insgestion-body.png

* Paste your xml Ingestion example within the text fields and click `Send`.
* The response from the request will be show as below.

   .. image:: images/postman-insgestion-response.png

* Copy the section of the `originatingWorkflowStep` and `reportableInformation` including the opening and closing brackets `{ }` before and after as shown below.

     {
          "originatingWorkflowStep": { *** },
          "reportableInformation": { *** }
     }

**Running Custom Function Service**

Using the results generated in above steps user can run it through `Custom Function Service` to generate `Reportable Event`

* Within Rosetta navigate to `DRR Model` and click on `API Export` at the Bottom of the screen click on the `run-function-service`. As shown below.

   .. image:: images/api-export-cust-func-service.png

* Copy the URL and Open new request in Postman. Set request type as `POST` and paste the URL copied in the step above and add `/drr.regulation.common.functions.Create_ReportableEventFromInstruction` at the end of the copied URL.
* Open the `Header` tab under column `Key` type `Authorization` and with in `Value` copy and paste the value from `API Key` from Rosetta Screen as shown in screen shot above.
* Open tab `Body` in Postman. Select option `raw` and type `JSON` as shown in above screen.

   .. image:: images/postman-cust-func-url-body.png

* Paste your copied response from the previous request(Ingestion Service) within the text fields and click `Send`.
* The response from the request will be show as below.

   .. image:: images/postman-cust-func-response.png

* Copy the response generated from the request.

**Running Regulation Report Service**

Using the results generated in above steps user can run it through `Regulation Report Service` to generate final `Report'.

* Within Rosetta navigate to `DRR Model` and click on `API Export` at the Bottom of the screen click on the `regulation-report-service` as shown below.

   .. image:: images/api-export-report-service.png

* Copy the URL and Open new request in Postman. Set request type as `POST` and paste the URL copied in the step above and add `/CFTC/Part45` at the end of the copied URL
* Open the `Header` tab under column `Key` type `Authorization` and with in `Value` copy and paste the value from `API Key` from Rosetta Screen as shown in screen shot above.
* Open tab `Body` in Postman. Select option `raw` and type `JSON`.
* Paste your copied response from the previous request(run-function-Service) within the text fields and click `Send`.
* The response from the request will be show as below.

   .. image:: images/postman-report-service-url-body-response.png
