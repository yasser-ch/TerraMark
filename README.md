# 🌿 TerraMark — Lab 13 : Localisation avec OpenStreetMap

## Objectif

Construire une application Android de géolocalisation utilisant **OpenStreetMap (OSMDroid)** pour afficher les positions sur une carte libre et gratuite, sans clé API. L'application récupère la position GPS, l'envoie à un serveur PHP/MySQL et affiche les positions enregistrées sous forme de marqueurs sur la carte.

---

## Concepts Abordés

- Intégration d'**OSMDroid** (OpenStreetMap pour Android)
- `LocationManager` et `LocationListener` pour le GPS
- Envoi de requêtes HTTP POST avec **Volley**
- Récupération de JSON avec `JsonObjectRequest`
- Ajout de marqueurs personnalisés sur une carte OSM
- Configuration de la sécurité réseau (`network_security_config`)
- Navigation entre deux activités
- Récupération de l'identifiant appareil (`ANDROID_ID`)

---

## Différence avec Google Maps

| Critère              | Google Maps        | OpenStreetMap (OSMDroid) |
|---------------------|--------------------|--------------------------|
| Clé API             | Obligatoire        | Non requise              |
| Coût                | Payant au-delà quota | Gratuit                |
| Données cartographiques | Google         | Communauté open-source   |
| Personnalisation    | Limitée            | Très flexible            |

---

## Aperçu de l'Application

### MainActivity
| Élément            | Description                                      |
|-------------------|--------------------------------------------------|
| Titre             | "TerraMark" en vert sur fond clair               |
| Carte GPS         | Affiche latitude et longitude en temps réel      |
| Statut            | Heure de mise à jour / statut envoi              |
| Bouton            | "Ouvrir la carte" → ouvre OpenMapActivity        |

### OpenMapActivity
| Élément            | Description                                      |
|-------------------|--------------------------------------------------|
| Titre             | "🗺️ Carte des positions"                        |
| Carte OSM         | Affiche tous les marqueurs depuis le serveur     |
| Contrôles         | Zoom intégré + pinch-to-zoom                     |

---

## DEMO 


https://github.com/user-attachments/assets/0fcaad15-3f29-42e8-8ff4-843246e1a0d4



## Structure du Projet

```
TerraMark/
├── java/com/example/terramark/
│   ├── MainActivity.java
│   └── OpenMapActivity.java
├── res/
│   ├── drawable/
│   │   └── marker.xml
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── activity_open_map.xml
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── xml/
│       └── network_security_config.xml
└── AndroidManifest.xml
```

---

## Permissions Requises

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

---

## Configuration Réseau

```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

> Nécessaire pour autoriser le trafic HTTP vers le serveur local depuis Android 9+.

---

## Détails Clés de l'Implémentation

### Initialisation OSMDroid
```java
Configuration.getInstance().load(
        getApplicationContext(),
        getSharedPreferences("terramark_prefs", MODE_PRIVATE));

osmMap.setTileSource(TileSourceFactory.MAPNIK);
osmMap.setBuiltInZoomControls(true);
osmMap.setMultiTouchControls(true);
osmMap.getController().setZoom(12.0);
osmMap.getController().setCenter(new GeoPoint(31.6295, -7.9811));
```

### Ajout de marqueurs personnalisés
```java
private void addMarkerToMap(double lat, double lon, int index) {
    Marker marker = new Marker(osmMap);
    marker.setPosition(new GeoPoint(lat, lon));
    marker.setTitle("Position " + index);
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    marker.setIcon(getScaledMarkerIcon());
    osmMap.getOverlays().add(marker);
}
```

### Gestion du cycle de vie
```java
@Override
protected void onResume() {
    super.onResume();
    osmMap.onResume();
}

@Override
protected void onPause() {
    super.onPause();
    osmMap.onPause();
}
```

---

## Structure du Serveur PHP (référence)

```
map_project/
├── createPosition.php
└── getPosition.php
```

### Table MySQL
```sql
CREATE TABLE positions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    date DATETIME NOT NULL,
    imei VARCHAR(50) NOT NULL
);
```

---

## Dépendances

```gradle
implementation 'com.android.volley:volley:1.2.1'
implementation 'org.osmdroid:osmdroid-android:6.1.16'
```

---

## Choix de Design

- **Thème :** Vert forêt / Fond clair
- **Palette de couleurs :** Vert (`#2E7D32`), Vert clair (`#81C784`), Fond (`#F9FBF9`)
- **Position par défaut :** Marrakech, Maroc (`31.6295, -7.9811`)

---

## Comment Exécuter

1. Cloner ou ouvrir le projet dans **Android Studio**
2. Vérifier que le Min SDK est défini à **24**
3. Lancer sur un émulateur ou appareil physique (Android 7.0+)
4. Accepter les permissions de localisation
5. Simuler une position via **Extended Controls → Location**
6. Appuyer sur **"Ouvrir la carte"** pour afficher la carte OSM

---

## Référence du Lab

- **Numéro du lab :** 13
- **Titre :** Création d'une Application de Localisation avec OpenStreetMap
- **Langage :** Java
- **Min SDK :** 24 (Android 7.0 Nougat)
- **Dépendances :** Volley 1.2.1, OSMDroid 6.1.16
