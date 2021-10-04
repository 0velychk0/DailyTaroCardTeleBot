# Taro Card of the Day in your Telegram

## Set Webhook URL for the bot

    https://api.telegram.org/bot2016087363:AAHDyBO8a-r9Av4jt4_X7DTVLu-nEeMWnFc/setWebhook?url=https://1025-188-163-102-141.ngrok.io

    {"ok":true,"result":true,"description":"Webhook was set"}

## Get Webhook URL for the bot

    https://api.telegram.org/bot2016087363:AAHDyBO8a-r9Av4jt4_X7DTVLu-nEeMWnFc/getWebhookInfo

    {"ok":true,"result":{"url":"https://www.google.com","has_custom_certificate":false,"pending_update_count":0,"max_connections":40,"ip_address":"142.251.36.4"}}

## Good example here :

    https://javarush.ru/groups/posts/3493-telegram-bot--napominalka-cherez-webhook-na-java-ili-skazhi-net-google-kalendarju

## How to deploy via google apps engine console
- Open console and run command to get your url

    gcloud app browse

- Put this URL to application.properties as telegrambot.webHookPath

- Set this url as Webhook URL

    https://api.telegram.org/bot2016087363:AAHDyBO8a-r9Av4jt4_X7DTVLu-nEeMWnFc/setWebhook?url=https://1025-188-163-102-141.ngrok.io

- Open console and run next commands to deploy

    git clone https://github.com/0velychk0/webhookTelegramBot.git

    cd webhookTelegramBot

    mvn -DskipTests package appengine:deploy
