# LCCInteractionSystem
## Run LCC service
1. Go to project root directory
```bash
cd /path/stores/project/LCCInteractionSystem 
```
2. Start LCC service

- simple version
```shell
./startengine.sh
```
- MySQL version
```shell
./startengine_mysql.sh
```
3. Go to game pages

- simple version
```URL
localhost/phpfile/ultimategame/welcome.php
```
- MySQL version
```URL
localhost/lccgame_mysql/ultimategame/welcome.php
```
or
```URL
localhost/lccgame_mysql/trustgame/welcome.php
```
## Stop LCC service
1. Go to project root directory
```shell
cd /path/stores/project/LCCInteractionSystem 
```
2. Stop LCC service
```shell
python stopengine.sh
```
