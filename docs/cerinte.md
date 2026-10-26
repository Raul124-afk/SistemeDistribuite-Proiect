# Documentație Proiect Sisteme Distribuite - Etapa 1

## 1. Cerințe Funcționale (Operații)

| Operație | Comandă | Pașii prin sistem | Rezultat | Erori tratate |
| :--- | :--- | :--- | :--- | :--- |
| **Încărcare** | `put raport.pdf` | 1. Clientul cere coordonatorului două noduri active, distincte.<br>2. Trimite fișierul primului nod.<br>3. Primul nod îl copiază pe al doilea.<br>4. După ambele confirmări, coordonatorul înregistrează metadatele. | ID-ul fișierului și cele două noduri | - Mai puțin de 2 noduri active: refuz, nimic stocat.<br>- Fișier local inexistent.<br>- O copie eșuată: se alege alt nod. |
| **Descărcare** | `get raport.pdf ./out/` | 1. Clientul cere coordonatorului locațiile copiilor.<br>2. Descarcă de la primul nod.<br>3. Verifică checksum-ul. | Fișierul salvat local, cu checksum verificat | - Fișier necunoscut.<br>- Primul nod nu răspunde sau checksum greșit: încearcă a doua copie.<br>- Nicio copie accesibilă: eroare explicită. |
| **Listare** | `ls` | 1. Clientul cere coordonatorului lista tuturor fișierelor stocate.<br>2. Coordonatorul trimite lista cu nume, dimensiune, checksum și nodurile care dețin copii. | Lista fișierelor stocate afișată în terminal | - Coordonator inaccesibil.<br>- Lista de fișiere este goală. |
| **Ștergere** | `rm raport.pdf` | 1. Clientul trimite cererea de ștergere coordonatorului.<br>2. Coordonatorul trimite comanda de ștergere nodurilor active care dețin fișierul.<br>3. Nodurile șterg fișierul din stocarea locală.<br>4. Dacă un nod este căzut, coordonatorul marchează copia ca restantă pentru ștergere ulterioară la reconectare. | Confirmarea ștergerii fișierului | - Fișier inexistent.<br>- Unul sau mai multe noduri sunt inaccesibile la momentul ștergerii. |
| **Starea sistemului** | `nodes` | 1. Clientul cere coordonatorului lista nodurilor din rețea.<br>2. Coordonatorul returnează ID-ul, adresa, portul, starea (ACTIV/SUSPECT/CAZUT) și specifică cine este coordonatorul actual. | Lista nodurilor și starea lor | - Coordonator inaccesibil. |
| **Adăugare nod nou** | *(automata)* | 1. Nodul nou pornește și anunță prezența prin mesaj UDP multicast.<br>2. Coordonatorul îl descoperă și îl adaugă în lista de noduri active.<br>3. Nodul nou devine disponibil pentru viitoarele încărcări de fișiere. | Nodul este integrat dinamic în cluster | - Port sau adresă deja ocupată.<br>- Eșec de comunicare cu coordonatorul. |

### Decizii de proiectare:
- Un fișier este identificat unic prin **ID (UUID)** generat de coordonator, dar utilizatorul poate folosi **numele fișierului**.
- Dacă se încarcă un fișier cu un nume deja existent, sistemul va suprascrie metadatele sau va returna o eroare configurabilă.
- Dimensiunea maximă permisă pentru un fișier este de **500 MB**.

---

## 2. Modelul de Date

### Metadatele fișierelor (gestionate de coordonator)
| Câmp | Tip Java | Exemplu | Stabilit de |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `3f2a9c1e-5b7d-4e8a-9c21-7d4e5f6a8b90` | Coordonator, la încărcare |
| `nume` | `String` | `raport.pdf` | Utilizator |
| `dimensiune` | `long` (octeți) | `482133` | Client |
| `checksum` | `String` (SHA-256) | `9f86d081...` | Client, verificat de fiecare nod |
| `locatiiCopii` | `List<String>` | `["n2", "n3"]` | Coordonator, după confirmări |

### Lista nodurilor (gestionată de coordonator)
| Câmp | Tip Java | Exemplu | Stabilit de |
| :--- | :--- | :--- | :--- |
| `idNod` | `String` | `n2` | Configurație / Nod |
| `adresa` / `port` | `String`, `int` | `127.0.0.1`, `7002` | Configurație / Nod |
| `stare` | `enum` | `ACTIV`, `SUSPECT`, `CAZUT` | Coordonator (mecanism Heartbeat) |
| `ultimulSemnDeViata` | `long` (ms) | `System.currentTimeMillis()` | Coordonator la fiecare Heartbeat |

---

## 3. Cerințe Nefuncționale și Obiective Măsurabile

| Cerință | Obiectiv măsurabil | Cum se măsoară | Etapa |
| :--- | :--- | :--- | :--- |
| **Integritate** | 0 fișiere cu checksum diferit din 100 de descărcări | Script care încarcă și descarcă 100 de fișiere | 2 |
| **Descoperire** | Un nod nou apare în lista celorlalți în cel mult 3 s | Diferența dintre momentele din loguri | 2 |
| **Detectarea defectelor** | Un nod oprit cu `kill -9` este marcat căzut în cel mult 5 s | Loguri cu timestamp pe fiecare nod | 3 |
| **Scalabilitate** | Debitul de încărcare cu 3 noduri crește față de 1 nod | 10 fișiere de 100 MB, timp total | 4 |
| **Coordonare** | Noul coordonator este ales în cel mult 10 s după căderea celuilalt | Loguri algoritm Bully | 5 |
| **Replicare** | Fiecare fișier are exact 2 copii pe noduri distincte | Comanda `ls` și verificarea directoarelor `data/` | 6 |
| **Disponibilitate** | După oprirea oricărui nod, 100% din fișiere rămân descărcabile | Descărcarea tuturor fișierelor după oprirea unui nod | 7 |

---

## 4. Scenariul Demonstrației (Test de acceptanță)

| Pas | Acțiunea operatorului | Ce trebuie să se vadă | Funcționează din |
| :--- | :--- | :--- | :--- |
| **1** | Pornește nodurile `n1`, `n2`, `n3` în terminale separate. | Fiecare nod îi descoperă pe ceilalți; `n1` devine coordonator. | Etapa 2 |
| **2** | Execută `put raport.pdf` din client. | ID-ul fișierului și cele două noduri distincte care îl dețin. | Etapa 2 |
| **3** | Execută `get raport.pdf` din client. | Fișierul este descărcat cu checksum-ul identic. | Etapa 2 |
| **4** | Pornește nodul `n4` fără a opri celelalte noduri. | `n4` apare în comanda `nodes`; poate fi folosit la stocare. | Etapa 4 |
| **5** | Execută `kill -9` pe un nod care deține `raport.pdf`. | Nodul este marcat căzut; `get raport.pdf` reușește din cealaltă copie. | Etapa 3 |
| **6** | Execută `kill -9` pe coordonator. | Nodurile rămase aleg un nou coordonator prin algoritmul Bully. | Etapa 5 |
| **7** | Execută `ls` și `get raport.pdf` după realegere. | Metadatele sunt reconstruite; fișierul rămâne accesibil. | Etapa 6 |

---

## 5. Schița Protocolului de Comunicare

| Mesaj | De la → către | Conținut | Răspuns așteptat |
| :--- | :--- | :--- | :--- |
| `ALLOCATE` | Client → Coordonator | nume, dimensiune | `NODES` cu ID fișier și 2 noduri, sau `ERROR` |
| `PUT` | Client → Nod | ID, nume, dimensiune, checksum, nod copie + octeți | `STORED` sau `ERROR` |
| `REPLICATE` | Nod → Nod | ID, nume, dimensiune, checksum + octeți | `STORED` sau `ERROR` |
| `COMMIT` | Nod → Coordonator | ID fișier, nodurile care îl dețin | `OK` |
| `LOCATE` | Client → Coordonator | nume sau ID | `LOCATIONS` cu metadatele, sau `NOT_FOUND` |
| `GET` | Client → Nod | ID fișier | Antet cu dimensiune + octeți, sau `NOT_FOUND` |
| `LIST` | Client → Coordonator | cerere lista fișiere | `FILE_LIST` cu toate metadatele |
| `DELETE` | Client → Coordonator | nume sau ID fișier | `DELETED` sau `ERROR` |