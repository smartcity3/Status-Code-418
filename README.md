# Mercury

## What Is This?
Το Mercury Citizen Platform είναι μια crowdsourced πλατφόρμα, με σκοπό να απλοποιήσει την αλληλεπίδραση μεταξύ πολιτών και δήμων σε προβλήματα που αφορούν τη τοπική αυτοδίοικηση, προσφέροντας πλήρη διαφάνεια και σεβασμό στα προσωπικά δεδομένα ενώ ταυτόχρονα προάγει τον εθελοντισμό.

## Components:
#####     -Blockchain based Issue Tracking Ledger.
#####     -Issue Reporting and Monitoring Android app.
#####     -Web Interface for both Citizen and Official use.

## Deployment Notes:
#### For the blockchain network:
Use a linux enviroment with Hypeledger Fabric and Hyperledger Composer installed at the latest versions.
##### To deploy the network:
Run the startFabric.sh and then run composer-playground. Select to deploy a new network, load the .bna file from your computer, fill out the admin options and you get running. Once done, run composer-rest-server to get the api online.

#### For the app:
Just change the IP address references to your Rest-API server IP.

#### For the WebApp:
Just open and use.
