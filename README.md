# Presently: A Gratitude Journal
[![CircleCI](https://circleci.com/gh/alisonthemonster/Presently/tree/develop.svg?style=svg)](https://circleci.com/gh/alisonthemonster/Presently/tree/develop)

A journal to write what you're thankful for. Contributions welcome!

[Google Play Posting](https://play.google.com/store/apps/details?id=journal.gratitude.com.gratitudejournal&hl=en)

### Philosophy 
Presently is built on the idea that gratitude journaling should be free, private, and available to all. It will never have ads, never have a premium version, and will always be open source. Entries are kept on device and can be exported to CSV.

Presently is also built with the best Android practices in mind. It follows MVVM to separate concerns and ensure we write testable code. We require 80%+ code coverage and rely on both unit tests and instrumented tests (with a heavier weight towards unit tests). Instrumented tests are run on Firebase Test Lab, crashes are reported through Crashlytics, and analytics are reported with Firebase Analytics.

We support several languages currently and are always looking for translators to help us! We support French, Spanish, Italian, German, Finnish, Arabic, Dutch, Polish, Portuguese, Slovakian, Croatian, Romanian, Turkish, Russian, Afrikaans.

### Tech Stack [So far...]
- MVVM with architecture components
   - ViewModel
   - Room (with FTS and Paging v3)
- Mavericks
- Navigation Component
- CircleCI
- Jacoco coverage reports (integrated with CI)
- Firebase Crashlytics Crash Reporting
- Dagger
- Espresso
- Firebase Test Lab
- WorkManager
- Dropbox Java SDK

<img src="https://i.imgur.com/Im3maBV.png" width="220">  <img src="https://i.imgur.com/O4J2yru.png" width="220">  <img src="https://i.imgur.com/TDCxl3N.png" width="220"> 

### Contributions
Contributions are welcome! Please fork the repo and make a PR. Forked PRs will build the app and run unit tests but will not run instrumented tests. A repo owner will run those tests as they require secret keys.
