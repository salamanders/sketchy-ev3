

## Deploy

```bash
# jar to bot, only when you make a change to client code
mvn deploy 
# Edit the contents of public/config.json (or whatever you put your script in)
firebase deploy # script to cloud, every time you want a new script
```