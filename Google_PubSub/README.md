1. Make sure python 2.7 is installed on your machine.
2. Install google cloud sdk:
    1. For macOS: https://cloud.google.com/sdk/docs/quickstart-macos
    2. For ubuntu: https://cloud.google.com/sdk/docs/quickstart-debian-ubuntu
3. Install the pubsub emulator: https://cloud.google.com/pubsub/docs/emulator
   1. `gcloud components install pubsub-emulator`
   2. `gcloud components update`
   3. Start the emulator on localhost: `gcloud beta emulators pubsub start`
   4. Set env variable: `gcloud beta emulators pubsub env-init`
4. Install google cloud python sdk: https://cloud.google.com/python/setup
    1. Create a virtualenv and activate it.
    2. Inside the virtual env, install google cloud pubsub sdk:
       1. `pip install --upgrade google-cloud-pubsub`
    3. Run `python client.py` and follow the steps as asked by the prompt.
        1. You can run multiple publishers and subscribers to see whats happening.



