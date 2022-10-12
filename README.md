# Lohnsteuer nach Programmablaufplan
Programmablaufplan f�r Lohnsteuer in BPMN mit Camunda 8 automatisiert

Hier ist der original [Programmablaufplan](https://www.bundesfinanzministerium.de/Content/DE/Downloads/Steuern/Steuerarten/Lohnsteuer/Programmablaufplan/2022-05-20-geaenderte-PAP-2022-anwendung-ab-01-06-2022-anlage-1.pdf?__blob=publicationFile&v=2) f�r die maschinelle Berechnung
der vom Arbeitslohn einzubehaltenden Lohnsteuer, des Solidarit�tszuschlags
und der Ma�stabsteuer f�r die Kirchenlohnsteuer f�r 2022 

## Vorgehensweise
Ich habe die Programmablaufpl�ne aus der o.g. Dokumentation ohne Anpassung in BPMN nachmodelliert. Die Symbole wurde wie folgt �bersetzt:
* Jeder Terminator (Oval) wird ein Ereignis (Start und Endereignis)
* Jede Operation (Rechteck) wird zu einer Aktivit�t. 
* Die Operationen sind ausnahmslos Zuweisungen, die als Name der Aktivit�t mit dem Verb "Setze" �bernommen wurden. 
* Die Unterprogrammausf�hrungen (Rechteck mit doppelten vertikalen Linien) werden zu Aufrufaktivit�ten.
* Die Verzweigungen (Raute) werden zu Exclusiven Oder-Gateways. 
* Die Anmerkungen werden zu Textannotationen und Gruppierungen.

Hier ist ein Original Ablaufplan und der entstandene BPMN Prozess:

![PAP MLSTJAHR](doc/images/PAP-MLSTJAHR.png)

<img alt="MLSTJAHR Unterprozess" src="doc/images/mlstjahr-unterprozess.png" width="100%"> 

Um die Prozesse in Camunda 8 ausf�hren zu k�nnen, m�ssen technische Attribute aus der Beschreibung eingef�gt werden. Hierbei bin ich wie folgt vorgegangen:
* Die Bedingung f�r die Verweigung wird immer in den "Ja" Sequenzfluss �bernommen. (z.B. `WVFRB < 0`)
* Der "Nein" Sequenzfluss bekommt die logisch negierte Bedingung.   (z.B. `WVFRB >= 0`)
* Die Aktivit�t wird als Service-Task mit der Taskdefinition `noop` versehen und die Zuweisung wird als Output Mapping angelegt (z.B. `<zeebe:output source="=JRE4 / 100" target="ZRE4J" />`). Es wurde nur ein Worker f�r jeden Service-Task genutzt. Dieser Worker tut nichts und schlie�t die Aktivit�t sofort ab (No Operation). Die Operation wird �ber das Output-Mapping ausschlie�lich im BPMN-Diagramm abgelegt und gepflegt. Ein Bild dazu befindet sich unten.
* Die Aufrufaktivit�t wird direkt mit dem Unterprozess konfiguriert. Die Ergebnisse des Unterprozesses werden immer zur�ckgegeben (z.B. `<zeebe:calledElement processId="MLSTJAHR-Unterprozess" propagateAllChildVariables="true" />`. Ein Bild dazu befindet sich unten.

F�r schnelles Feedback bei der Modellierung habe ich einen JUnit-Test erstellt, der den Lohnsteuerprozess durchl�uft und diesen Test sukessive um  Eingabevariablen erweitert.

Die Baumstruktur mit den geschachtelten Unterprogrammen habe ich St�ck f�r St�ck, Unterprozess f�r Unterprozess umgesetzt. Der erste Prozess war der Lohsteuerprozess selbst. F�r die Unterprozesse habe ich als Platzhalter zuerst einen NOOP-Unterprozess ohne Aktivit�ten genutzt.

Die Unterprozesse bin ich nach der Depth-First Methode angegangen, ausgehend vom Hauptprozess.

Sobald ein Unterprozess ohne Fehler im Web-Modeler erstellt war, habe ich ihn exportiert, in mein Projekt �bernommen und den Oberprozess angepasst. Mit dem JUnit Test bekam ich schnelles Feedback, ob der neue Unterprozess erfolgreich durchlaufen wurde. Die generierte Testabdeckung zeigte den Pfad durch den Prozess.

Wenn der Test nicht erfolgreich war, half die generierte Testabdeckung festzustellen, wo der Prozessablauf abgebrochen ist. Die h�ufigsten Fehlerquellen waren fehlende Eingabedaten (im Testcode erg�nzt) und fehlende Datenr�ckgabe bei Unterprozessen (muss f�r jede Aufrufaktivit�t im Modeler aktiviert werden). Au�erdem sind in den PDF-Vorlagen an einigen Stellen Bindestriche f�r Minuszeichen genutzt worden. 

Im Programmablaufplan MZTABFB fehlt die Initialisierung der Variable `EFA`. Hier habe ich eine Aktivit�t hinzugef�gt, um den Wert vorher auf `0` zu setzen.   


## Testabdeckung
Der Prozess ist als Spring-Zeebe-Test mit Testabdeckung [hier zu finden](https://htmlpreview.github.io/?https://github.com/ingorichtsmeier/lohnsteuer-pap/blob/master/doc/testcoverage-static/com.camunda.consulting.lohnsteuer.TestLohnsteuerHauptprozess/report.html)

## Dauer
Ich habe insgesamt 2,5 Arbeitstage an den Prozessmodellen samt Test gearbeitet. An den Tagen habe ich auch die wiederkehrenden T�tigkeiten wie Emailbearbeitung durchgef�hrt. Gesch�tzt habe ich 13 Stunden an dem Projekt gearbeitet, bei 26 Modellen ergibt das im Schnitt 30 Minuten pro Unterprozess. 

Es war im nachhinein Vorteilhaft, alle Prozesse am St�ck umzusetzen. Mit jedem Programmablaufplan habe ich mehr �ber die Lohnsteuer gelernt. Die Details wie Variablen- und Unterprgrammnamen kann man sich als Nicht-Cobol-Programmierer nur schwer merken. Bei l�ngeren Pausen zwischen den Arbeiten (z.B. 1 Tag pro Woche) w�ren die R�stzeiten f�r das eigene Gehirn deutlich l�nger und man muss l�nger nach bereits abgeschlossenen Teilen und genutzten Konventionen suchen.

## Vorteile bei der Umsetzung der Programmablaufpl�ne mit BPMN
* Sichtbare Nachverfolgung der ausgef�hrten Operationen
* Direkte Umsetzung ohne zus�tzliche Programmierung m�glich
* Sehr gut testbar
* Ausf�hrung auf einer skalierbaren Umgebung (Camunda 8)
* Der Camunda Web-Modeler zeigt die Beziehungen zwischen Unterprozessen sehr gut auf:
    - Aus dem Oberprozess kann direkt in den Unterprozess gesprungen werden
    - Ein Unterprozess zeigt alle Oberprozesse, in denen er genutzt wurde.  

## Potentielle �berarbeitung
Die bisherige Modellierung setzt die Programmablaufpl�ne Eins-zu-Eins um. Dabei bleibt die Nomenklatur der Cobol-Programme und -Variablen erhalten.

Mit einer ausreichenden Testabdeckung kann man sich daran machen, die Prozesse zu �berarbeiten.

Ideen dazu:
* Variablen mit sprechenden Namen versehen
* Unterprozesse mit sprechenden Namen versehen
* einige Operationen in einem Service Task zusammenfassen und diesen besser benennen. Dabei k�nnen die Steuergesetze als Grundlage genommen werden.

## Modellierungsdetails

* Bedingungen im Modeler

![Ja-Sequence-Flow](doc/images/Ja-Sequence-Flow.png)

* Unterprozesse

![Call-Acticity](doc/images/Call-Activity.png)

* Aktivit�t mit Operation

![Service-Task](doc/images/Service-Task.png)

* Nutzung in Oberprozessen

![Nutzung in Oberprozessen](doc/images/Nutzung-in-Oberprozessen.png)

* Sprung in den Unterprozess

![Verbindung zum Unterprozess](doc/images/Verbindung-zum-Unterprozess.png)
