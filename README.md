Manuel d'Installation et d'Utilisation
Analyse et Identification de Modules d'une Application Java avec Spoon

📋 Table des Matières

Description du Projet
Prérequis
Installation
Configuration
Utilisation
Fonctionnalités
Résolution des Problèmes
Structure du Projet


📝 Description
Cette application permet d'analyser la structure d'un projet Java en :

Extrayant les graphes d'appels entre méthodes
Calculant les métriques de couplage entre classes
Identifiant automatiquement les modules via clustering hiérarchique (HAC)
Générant des visualisations (dendrogrammes, graphes de couplage)
Utilisant Spoon pour l'analyse statique du code source


🔧 Prérequis
Logiciels Requis

Java JDK 11 ou supérieur (JDK 17 recommandé)
Eclipse IDE (version 2021-06 ou ultérieure)
Git (optionnel, pour cloner le projet)

Bibliothèques Externes (JAR)
Vous devez avoir les fichiers JAR suivants :

Spoon Core (spoon-core-10.4.2.jar ou version similaire)

Téléchargement : https://github.com/INRIA/spoon/releases
Ou Maven Central : https://mvnrepository.com/artifact/fr.inria.gforge.spoon/spoon-core


Eclipse JDT Core (org.eclipse.jdt.core-3.x.x.jar)

Inclus avec Eclipse ou téléchargeable séparément
Maven Central : https://mvnrepository.com/artifact/org.eclipse.jdt/org.eclipse.jdt.core


Dépendances de Spoon (si nécessaire) :

slf4j-api-x.x.x.jar
slf4j-simple-x.x.x.jar (pour les logs)




📦 Installation
Étape 1 : Télécharger le Projet
Option A : Cloner depuis Git
bashgit clone https://github.com/BenBoubakerMajdi/GraphCouplage-Spoon-JavaProject.git
cd tp2-analyse-modules
Option B : Télécharger le ZIP

Téléchargez l'archive ZIP du projet
Extrayez-la dans un dossier de votre choix

Étape 2 : Importer dans Eclipse

Ouvrez Eclipse IDE
Menu : File → Import...
Sélectionnez : General → Existing Projects into Workspace
Cliquez sur Next
Choisissez Select root directory et naviguez vers le dossier du projet
Cochez le projet dans la liste
Cliquez sur Finish

Étape 3 : Ajouter les JAR au Build Path

Clic droit sur le projet → Properties
Sélectionnez Java Build Path
Onglet Libraries
Cliquez sur Add JARs...
Naviguez vers lib/ et sélectionnez tous les JAR
Cliquez sur Apply and Close



Étape 4 : Vérifier la Configuration Java

Clic droit sur le projet → Properties
Java Compiler → Vérifiez que le niveau est 11 ou supérieur
Si nécessaire, cochez Enable project specific settings
Réglez Compiler compliance level sur 11 ou 17


⚙️ Configuration
Configurer le Chemin Source à Analyser
Avant d'exécuter l'application, vous devez spécifier le chemin du projet Java à analyser.

Ouvrez le fichier : src/Parser/ParserConfig.java
Modifiez la constante PROJECT_SOURCE_PATH :

javapackage Parser;

public class ParserConfig {
    // IMPORTANT : Remplacez par le chemin de votre projet à analyser
    public static final String PROJECT_SOURCE_PATH = 
        "C:/Users/VotreNom/workspace/MonProjetJava/src";
    
    // Exemples :
    // Windows : "C:/Projects/MyApp/src"
    // Linux/Mac : "/home/user/projects/myapp/src"
}

Points importants :

Utilisez des slashes / (pas de backslashes \)
Le chemin doit pointer vers le dossier src contenant les packages Java
Vérifiez que le chemin existe et contient des fichiers .java




🚀 Utilisation
Lancer l'Application

Ouvrez src/Parser/Parser.java
Clic droit sur le fichier → Run As → Java Application
Le Metrics Dashboard s'ouvre automatiquement

Interface Principale : Metrics Dashboard
L'application affiche un tableau de bord avec deux onglets :
Onglet "Dashboard"
Affiche les métriques du projet analysé :
MétriqueDescriptionTotal ClassesNombre total de classes dans le projetTotal Application LinesNombre total de lignes de codeTotal MethodsNombre total de méthodesTotal PackagesNombre de packagesAvg Methods/ClassMoyenne de méthodes par classeAvg Lines/MethodMoyenne de lignes par méthodeAvg Attributes/ClassMoyenne d'attributs par classe
Filtrage par Package

Utilisez le menu déroulant en haut pour filtrer par package
Cliquez sur Apply Filter pour actualiser les métriques

Boutons d'Action

Show Call Graph 🔵

Affiche le graphe d'appels entre méthodes
Visualisation interactive circulaire


Show All Modules (JDT) 🟢

Identifie et liste tous les modules via JDT
Affiche les modules dans une fenêtre popup


Spoon Analysis (HAC) 🔴

Lance l'analyse complète avec Spoon
Génère le dendrogramme et liste les modules


Show Dendrogram (JDT) 🟣

Affiche le dendrogramme basé sur l'analyse JDT
Visualisation hiérarchique du clustering



Onglet "AST Logs"
Affiche les logs détaillés de l'analyse syntaxique :

Méthodes détectées
Variables analysées
Invocations de méthodes


🎯 Fonctionnalités
1. Graphe d'Appels (Call Graph)
Action : Cliquez sur Show Call Graph
Résultat :

Graphe circulaire avec les classes comme nœuds
Arêtes colorées représentant les appels (opacité = intensité)
Labels rouges pour les couplages > 0.1

Interprétation :

Plus l'arête est visible, plus le couplage est fort
Classes proches = forte dépendance

2. Matrice de Couplage
Calcul automatique lors de l'analyse
Formule :
Couplage(A,B) = Nombre d'appels bidirectionnels / Total appels
Valeurs :

0.0 = Aucun couplage
> 0.5 = Couplage très fort
1.0 = Couplage maximal

3. Clustering Hiérarchique (HAC)
Action : Cliquez sur Spoon Analysis (HAC) ou Show Dendrogram (JDT)
Algorithme :

Initialisation : chaque classe = 1 cluster
Itération : fusion des 2 clusters les plus couplés
Arrêt : 1 seul cluster (racine du dendrogramme)

Paramètres :

CP (Coupling Parameter) : Seuil de couplage (défaut = 0.02)
Modifiable dans le code :

java  ModuleIdentifier.buildDendrogram(matrix, 0.02); // Changer 0.02
4. Dendrogramme
Visualisation :

Feuilles : Classes individuelles en bas
Lignes horizontales : Points de fusion
Valeurs rouges : Couplage lors de la fusion
Plus la fusion est haute, plus le couplage est fort

Interprétation :

Classes fusionnées tôt = Fortement couplées
Branches distinctes = Modules indépendants

5. Identification de Modules
Résultat :
=== SPOON MODULES (HAC) ===
Total modules: 4

Module 1 (3 classes):
  - Parser.ParserLogic
  - Parser.ParserConfig
  - Parser.Parser

Module 2 (5 classes):
  - TP2.CouplingGraph
  - TP2.ModuleIdentifier
  - TP2.SpoonAnalyzer
  ...
Critères :

Couplage interne élevé
Couplage externe faible
Cohésion fonctionnelle


🛠️ Résolution des Problèmes
Problème 1 : --module-path Error
Erreur :
java.lang.IllegalArgumentException: Unrecognized option: --module-path
Solution :

Ouvrez src/TP2/SpoonAnalyzer.java
Vérifiez la ligne :

java   launcher.getEnvironment().setComplianceLevel(11); // PAS 17

Assurez-vous que c'est bien 11 et non 17

Problème 2 : Dendrogramme Vide ou Incomplet
Causes possibles :

Classes filtrées (externes)
Aucun appel entre classes

Solutions :

Vérifiez les logs console :

   Project classes: X
   Extracted Y method calls

Si Y = 0, vérifiez que :

Le chemin source est correct
Les classes contiennent des méthodes qui s'appellent


Réduisez le seuil CP :

java   ModuleIdentifier.buildDendrogram(matrix, 0.01); // Au lieu de 0.02

📁 Structure du Projet
TP2-AnalyseModules/
│
├── src/
│   ├── Parser/
│   │   ├── Main.java                    # Point d'entrée
│   │   ├── ParserConfig.java            # Configuration (CHEMIN SOURCE)
│   │   ├── ParserLogic.java             # Logique JDT
│   │   ├── MetricsDashboardFrame.java   # Interface principale
│   │   └── CallGraphFrame.java          # Graphe d'appels
│   │
│   └── TP2/
│       ├── Call.java                     # Record pour appels
│       ├── CouplingGraph.java            # Matrice de couplage
│       ├── ModuleIdentifier.java         # Algorithme HAC
│       ├── SpoonAnalyzer.java            # Analyse Spoon
│       ├── DendrogramFrame.java          # Fenêtre dendrogramme
│       ├── DendrogramPanel.java          # Visualisation dendrogramme
│       └── CouplingGraphFrame.java       # Graphe circulaire
│
│
├── bin/                                  # Fichiers .class compilés
│
├── README.md                             # Ce fichier
└── .classpath                            # Configuration Eclipse

Dendrogramme s'affiche
Popup liste les 8 modules identifiés


Analyser le dendrogramme :

Classes proches = Même module potentiel
Valeurs rouges élevées = Fort couplage


Ajuster le seuil si nécessaire :

CP trop bas → Trop de modules
CP trop haut → Pas assez de modules
Valeur recommandée : entre 0.01 et 0.05


🎓 Références
Documentation Spoon

Site officiel : https://spoon.gforge.inria.fr/
GitHub : https://github.com/INRIA/spoon
Tutoriels : https://spoon.gforge.inria.fr/tutorials.html

Algorithmes

HAC (Hierarchical Agglomerative Clustering) :

Murtagh, F. & Contreras, P. (2012). Algorithms for hierarchical clustering


Métriques de couplage :

Chidamber & Kemerer (1994). A metrics suite for object-oriented design



Eclipse JDT

Documentation : https://www.eclipse.org/jdt/
API : https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.isv/
