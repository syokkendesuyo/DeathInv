DeathInv
========

Inventory logging on player death.

プレイヤーの死亡時に、インベントリをログに残すプラグイン

【コマンド】

コマンドは deathinv、省略形は di、パーミッションノードは deathinv（OPはデフォルトで利用可）です。

/di list (player) [(page)] - playerの死亡ログ一覧を表示します。

/di show (player) (id) - playerのidにおける死亡ログ詳細を表示します。

/di get (player) (id) - playerのidにおける死亡ログから、インベントリを取得し、自分のインベントリに反映されます。自分のインベントリは消去されるので注意してください。

/di temp - 自分のインベントリを、一時的に保存します。一時保存先は、サーバーがreloadするまで有効です。

/di restore - 自分のインベントリを、一時的保存先から復帰します。

