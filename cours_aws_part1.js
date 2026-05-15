const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        HeadingLevel, AlignmentType, BorderStyle, WidthType, ShadingType,
        PageNumber, Header, Footer, TableOfContents, PageBreak,
        LevelFormat, VerticalAlign } = require('docx');
const fs = require('fs');

const CONTENT_WIDTH = 9360;
const COL2 = 4680;
const ORANGE = "E8630A";
const BLUE = "1B4F8A";
const LIGHT_BLUE = "D6E4F0";
const LIGHT_ORANGE = "FEF0E6";
const LIGHT_GREEN = "E8F8E8";
const LIGHT_RED = "FDECEA";
const LIGHT_GRAY = "F5F5F5";
const DARK_GRAY = "333333";

const border = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
const borders = { top: border, bottom: border, left: border, right: border };
const noBorders = {
  top: { style: BorderStyle.NONE, size: 0, color: "FFFFFF" },
  bottom: { style: BorderStyle.NONE, size: 0, color: "FFFFFF" },
  left: { style: BorderStyle.NONE, size: 0, color: "FFFFFF" },
  right: { style: BorderStyle.NONE, size: 0, color: "FFFFFF" }
};

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    children: [new TextRun({ text, bold: true, color: BLUE })]
  });
}
function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    children: [new TextRun({ text, bold: true, color: BLUE })]
  });
}
function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    children: [new TextRun({ text, bold: true })]
  });
}
function h4(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_4,
    children: [new TextRun({ text, bold: true })]
  });
}
function p(text, opts = {}) {
  return new Paragraph({
    spacing: { after: 160 },
    children: [new TextRun({ text, ...opts })]
  });
}
function pageBreak() {
  return new Paragraph({ children: [new PageBreak()] });
}
function spacer() {
  return new Paragraph({ spacing: { after: 200 }, children: [new TextRun("")] });
}

function bullet(text, bold = false) {
  return new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    spacing: { after: 80 },
    children: [new TextRun({ text, bold })]
  });
}

function numbered(text) {
  return new Paragraph({
    numbering: { reference: "numbers", level: 0 },
    spacing: { after: 80 },
    children: [new TextRun({ text })]
  });
}

function codeBlock(lines) {
  const children = [];
  lines.forEach((line, i) => {
    children.push(new TableRow({
      children: [new TableCell({
        borders: noBorders,
        width: { size: CONTENT_WIDTH - 240, type: WidthType.DXA },
        shading: { fill: "1E1E1E", type: ShadingType.CLEAR },
        margins: { top: 0, bottom: 0, left: 200, right: 200 },
        children: [new Paragraph({
          spacing: { after: 0, before: 0, line: 276 },
          children: [new TextRun({ text: line || " ", font: "Courier New", size: 18, color: "D4D4D4" })]
        })]
      })]
    }));
  });
  return new Table({
    width: { size: CONTENT_WIDTH - 240, type: WidthType.DXA },
    columnWidths: [CONTENT_WIDTH - 240],
    margins: { top: 160, bottom: 160 },
    borders: {
      top: { style: BorderStyle.SINGLE, size: 2, color: "555555" },
      bottom: { style: BorderStyle.SINGLE, size: 2, color: "555555" },
      left: { style: BorderStyle.SINGLE, size: 2, color: "555555" },
      right: { style: BorderStyle.SINGLE, size: 2, color: "555555" }
    },
    rows: children
  });
}

function infoBox(title, text, fill = LIGHT_BLUE, titleColor = BLUE) {
  return new Table({
    width: { size: CONTENT_WIDTH, type: WidthType.DXA },
    columnWidths: [CONTENT_WIDTH],
    rows: [
      new TableRow({ children: [new TableCell({
        borders,
        width: { size: CONTENT_WIDTH, type: WidthType.DXA },
        shading: { fill, type: ShadingType.CLEAR },
        margins: { top: 120, bottom: 120, left: 200, right: 200 },
        children: [
          new Paragraph({ spacing: { after: 60 }, children: [new TextRun({ text: title, bold: true, color: titleColor, size: 22 })] }),
          new Paragraph({ spacing: { after: 0 }, children: [new TextRun({ text, size: 20 })] })
        ]
      })] })
    ]
  });
}

function twoColTable(headers, rows) {
  const headerRow = new TableRow({
    children: headers.map((h, i) => new TableCell({
      borders,
      width: { size: i === 0 ? 3000 : CONTENT_WIDTH - 3000, type: WidthType.DXA },
      shading: { fill: BLUE, type: ShadingType.CLEAR },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({ children: [new TextRun({ text: h, bold: true, color: "FFFFFF", size: 20 })] })]
    }))
  });
  const dataRows = rows.map((row, ri) => new TableRow({
    children: row.map((cell, ci) => new TableCell({
      borders,
      width: { size: ci === 0 ? 3000 : CONTENT_WIDTH - 3000, type: WidthType.DXA },
      shading: { fill: ri % 2 === 0 ? "FFFFFF" : LIGHT_GRAY, type: ShadingType.CLEAR },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({ spacing: { after: 0 }, children: [new TextRun({ text: cell, size: 20 })] })]
    }))
  }));
  return new Table({
    width: { size: CONTENT_WIDTH, type: WidthType.DXA },
    columnWidths: [3000, CONTENT_WIDTH - 3000],
    rows: [headerRow, ...dataRows]
  });
}

function multiColTable(headers, colWidths, rows) {
  const total = colWidths.reduce((a, b) => a + b, 0);
  const headerRow = new TableRow({
    children: headers.map((h, i) => new TableCell({
      borders,
      width: { size: colWidths[i], type: WidthType.DXA },
      shading: { fill: BLUE, type: ShadingType.CLEAR },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({ spacing: { after: 0 }, children: [new TextRun({ text: h, bold: true, color: "FFFFFF", size: 20 })] })]
    }))
  });
  const dataRows = rows.map((row, ri) => new TableRow({
    children: row.map((cell, ci) => new TableCell({
      borders,
      width: { size: colWidths[ci], type: WidthType.DXA },
      shading: { fill: ri % 2 === 0 ? "FFFFFF" : LIGHT_GRAY, type: ShadingType.CLEAR },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({ spacing: { after: 0 }, children: [new TextRun({ text: cell, size: 20 })] })]
    }))
  }));
  return new Table({
    width: { size: total, type: WidthType.DXA },
    columnWidths: colWidths,
    rows: [headerRow, ...dataRows]
  });
}

// ============================================================
// MODULE 1: Architecture AWS & Application Web
// ============================================================
function module1() {
  return [
    pageBreak(),
    h1("MODULE 1 : Création d'une Application Web sur AWS"),
    spacer(),
    p("AWS (Amazon Web Services) est la plateforme cloud la plus utilisée au monde, offrant plus de 200 services managés permettant de construire des applications web robustes, scalables et hautement disponibles. Ce module explore l'architecture d'une application web moderne sur AWS, les services fondamentaux à connaître et comment les assembler pour créer une solution complète.", { size: 22 }),
    spacer(),

    h2("1.1 Objectifs d'Apprentissage"),
    bullet("Comprendre l'architecture d'une application web moderne sur AWS"),
    bullet("Identifier les services AWS nécessaires pour chaque couche applicative"),
    bullet("Concevoir une architecture hautement disponible et tolérante aux pannes"),
    bullet("Comprendre les modèles de déploiement cloud (IaaS, PaaS, SaaS)"),
    bullet("Appliquer les bonnes pratiques du AWS Well-Architected Framework"),
    spacer(),

    infoBox("Astuce Certification DVA-C02",
      "L'examen AWS Certified Developer Associate teste votre capacité à choisir les bons services AWS pour des cas d'usage specifiques. Connaissez les limites et caracteristiques de chaque service : S3 (objets jusqu'a 5 TB), Lambda (timeout max 15 min, memoire 128MB-10GB), DynamoDB (items jusqu'a 400KB). Ces chiffres apparaissent frequemment dans les questions.",
      LIGHT_ORANGE, ORANGE),
    spacer(),

    h2("1.2 Qu'est-ce qu'AWS ?"),
    p("Amazon Web Services est une filiale d'Amazon qui fournit des services d'infrastructure cloud a la demande depuis 2006. AWS propose une infrastructure mondiale composee de regions, zones de disponibilite (AZ) et points de presence (PoP) distribues sur tous les continents."),
    p("Au 2025, AWS dispose de :"),
    bullet("33 regions geographiques actives dans le monde"),
    bullet("105 zones de disponibilite (AZs)"),
    bullet("Plus de 450 points de presence CloudFront"),
    bullet("Plus de 200 services et produits"),
    spacer(),

    h2("1.3 Le Modele de Responsabilite Partagee"),
    p("Un concept fondamental d'AWS est le modele de responsabilite partagee. AWS est responsable de la securite 'du' cloud (infrastructure physique, hyperviseurs, reseaux), tandis que le client est responsable de la securite 'dans' le cloud (donnees, applications, configurations IAM, chiffrement)."),
    spacer(),
    multiColTable(
      ["Responsabilite", "AWS", "Client"],
      [2800, 3280, 3280],
      [
        ["Infrastructure physique", "Oui - datacenters, serveurs, reseaux", "Non"],
        ["Systeme d'exploitation (EC2)", "Non (hyperviseur seulement)", "Oui - patching OS"],
        ["Donnees clients", "Non", "Oui - chiffrement, acces"],
        ["Gestion des identites", "Non", "Oui - IAM, MFA"],
        ["Reseau virtuel (VPC)", "Infrastructure sous-jacente", "Configuration, SG, NACL"],
        ["Services manages (RDS, Lambda)", "OS, patching, infra", "Config, code, donnees"],
      ]
    ),
    spacer(),

    h2("1.4 Architecture d'une Application Web sur AWS"),
    p("Une application web moderne sur AWS suit generalement une architecture en couches qui separe les responsabilites et permet une scalabilite independante de chaque composant."),
    spacer(),
    h3("1.4.1 Architecture de Reference : Application Three-Tier"),
    spacer(),
    codeBlock([
      "┌─────────────────────────────────────────────────────────────────┐",
      "│                         INTERNET                                │",
      "└─────────────────────────┬───────────────────────────────────────┘",
      "                          │",
      "┌─────────────────────────▼───────────────────────────────────────┐",
      "│                    Route 53 (DNS)                               │",
      "│              Gestion des noms de domaine                        │",
      "└─────────────────────────┬───────────────────────────────────────┘",
      "                          │",
      "┌─────────────────────────▼───────────────────────────────────────┐",
      "│                CloudFront (CDN)                                 │",
      "│         Distribution de contenu statique global                 │",
      "└─────────────────────────┬───────────────────────────────────────┘",
      "                          │",
      "┌─────────────────────────▼───────────────────────────────────────┐",
      "│  COUCHE PRESENTATION                                            │",
      "│  ┌──────────────────────────────────────────────────────┐      │",
      "│  │  Application Load Balancer (ALB)                     │      │",
      "│  │  Distribution du trafic - Health checks              │      │",
      "│  └──────────────────────────────────────────────────────┘      │",
      "│              AZ-1              AZ-2              AZ-3          │",
      "│         ┌─────────┐      ┌─────────┐      ┌─────────┐         │",
      "│         │ EC2/ECS │      │ EC2/ECS │      │ EC2/ECS │         │",
      "│         │Frontend │      │Frontend │      │Frontend │         │",
      "│         └─────────┘      └─────────┘      └─────────┘         │",
      "└─────────────────────────┬───────────────────────────────────────┘",
      "                          │",
      "┌─────────────────────────▼───────────────────────────────────────┐",
      "│  COUCHE LOGIQUE METIER                                          │",
      "│         ┌─────────┐      ┌─────────┐      ┌─────────┐         │",
      "│         │  Lambda │      │ ECS/EKS │      │   EC2   │         │",
      "│         │Serverl. │  ou  │Conteneu.│  ou  │Tradition│         │",
      "│         └─────────┘      └─────────┘      └─────────┘         │",
      "│              API Gateway / Application Load Balancer            │",
      "└─────────────────────────┬───────────────────────────────────────┘",
      "                          │",
      "┌─────────────────────────▼───────────────────────────────────────┐",
      "│  COUCHE DONNEES                                                 │",
      "│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌──────────┐  │",
      "│  │  RDS/Aurora│  │  DynamoDB  │  │    S3      │  │ ElastiC. │  │",
      "│  │  SQL/ACID  │  │  NoSQL     │  │  Fichiers  │  │  Cache   │  │",
      "│  └────────────┘  └────────────┘  └────────────┘  └──────────┘  │",
      "└─────────────────────────────────────────────────────────────────┘",
    ]),
    spacer(),

    h3("1.4.2 Description des Couches"),
    p("Chaque couche de l'architecture a un role specifique :"),
    spacer(),
    twoColTable(
      ["Couche", "Description et Services"],
      [
        ["DNS & CDN", "Route 53 gere les enregistrements DNS avec health checks automatiques. CloudFront distribue le contenu statique depuis 450+ PoP mondiaux avec latence minimale."],
        ["Load Balancing", "L'Application Load Balancer (ALB) distribue le trafic HTTP/HTTPS, effectue des health checks et supporte le routage base sur les chemins URL et headers."],
        ["Compute", "EC2 pour les VM traditionnelles, ECS/EKS pour les conteneurs Docker/Kubernetes, Lambda pour le serverless. Chaque option a ses avantages en terme de cout et gestion."],
        ["API Management", "API Gateway expose les APIs REST ou WebSocket, gere l'authentification, le rate limiting, le caching et la transformation des requetes/reponses."],
        ["Base de donnees", "RDS/Aurora pour les bases SQL ACID, DynamoDB pour le NoSQL haute performance, ElastiCache (Redis/Memcached) pour le cache en memoire."],
        ["Stockage objet", "S3 pour le stockage d'objets illimite, fiable a 99.999999999% (11 neufs), avec versioning, chiffrement et hosting de sites statiques."],
      ]
    ),
    spacer(),

    h2("1.5 Les Regions et Zones de Disponibilite"),
    p("La haute disponibilite sur AWS repose sur la distribution des ressources dans plusieurs zones de disponibilite au sein d'une region. Une region AWS est une zone geographique independante contenant au minimum 3 AZs."),
    spacer(),
    codeBlock([
      "Region AWS : eu-west-1 (Irlande)",
      "  ├── AZ: eu-west-1a  ─── Datacenter A (alimentation electrique independante)",
      "  ├── AZ: eu-west-1b  ─── Datacenter B (alimentation electrique independante)",
      "  └── AZ: eu-west-1c  ─── Datacenter C (alimentation electrique independante)",
      "  ",
      "Chaque AZ est connectee aux autres par un reseau fibre prive a faible latence (<2ms)",
      "Une panne dans eu-west-1a n'affecte pas eu-west-1b ou eu-west-1c",
    ]),
    spacer(),
    infoBox("Bonne Pratique",
      "Deployez toujours vos ressources critiques dans au minimum 2 zones de disponibilite. Pour une haute disponibilite de niveau production, utilisez 3 AZs. Cela protege contre les pannes d'un datacenter entier sans impact sur vos utilisateurs.",
      LIGHT_GREEN, "2E7D32"),
    spacer(),

    h2("1.6 Le AWS Well-Architected Framework"),
    p("AWS a defini 6 piliers fondamentaux pour concevoir des architectures robustes, appeles le Well-Architected Framework. Ces piliers sont evalues lors de la certification AWS Solutions Architect."),
    spacer(),
    multiColTable(
      ["Pilier", "Description", "Services Cles"],
      [2000, 4360, 3000],
      [
        ["Excellence Operationnelle", "Executer et surveiller les systemes, amelioration continue", "CloudWatch, CloudTrail, AWS Config"],
        ["Securite", "Proteger les donnees, systemes et actifs", "IAM, KMS, GuardDuty, WAF, Shield"],
        ["Fiabilite", "Recuperer des pannes, repondre a la demande", "Auto Scaling, Multi-AZ, Route 53"],
        ["Performance", "Utiliser efficacement les ressources", "CloudFront, ElastiCache, RDS Read Replicas"],
        ["Optimisation Couts", "Eviter les depenses inutiles", "Cost Explorer, Trusted Advisor, Spot Instances"],
        ["Durabilite", "Minimiser l'impact environnemental", "AWS Graviton, regions vertes, efficacite energetique"],
      ]
    ),
    spacer(),

    h2("1.7 Modeles de Deploiement Cloud"),
    p("AWS supporte differents modeles de deploiement selon les besoins en controle et gestion :"),
    spacer(),
    h3("1.7.1 IaaS - Infrastructure as a Service"),
    p("Vous gerez : OS, middleware, runtime, applications, donnees. AWS gere : virtualisation, serveurs, stockage, reseau."),
    p("Exemple : Amazon EC2. Vous installez Java, configurez Tomcat, deployer votre WAR."),
    spacer(),
    h3("1.7.2 PaaS - Platform as a Service"),
    p("Vous gerez : applications et donnees. AWS gere tout le reste."),
    p("Exemple : AWS Elastic Beanstalk. Vous uploadez votre JAR/WAR, AWS gere le reste automatiquement."),
    spacer(),
    h3("1.7.3 FaaS - Function as a Service (Serverless)"),
    p("Vous gerez : code de la fonction uniquement. AWS gere tout le reste y compris les serveurs."),
    p("Exemple : AWS Lambda. Vous ecrivez votre methode Java, AWS execute et scale automatiquement."),
    spacer(),
    infoBox("Comparaison des modeles",
      "IaaS: Controle maximum, gestion maximum. Cout variable. Bon pour: migration lift-and-shift.\nPaaS: Controle moyen, gestion reduite. Bon pour: applications web standard.\nFaaS: Controle minimal, zero gestion serveur. Facturation a la milliseconde. Bon pour: fonctions evenementielles.",
      LIGHT_BLUE, BLUE),
    spacer(),

    h2("1.8 Services AWS Fondamentaux pour une Application Web"),
    p("Voici un panorama complet des services AWS essentiels regroupes par categorie :"),
    spacer(),
    h3("1.8.1 Services de Calcul"),
    twoColTable(
      ["Service", "Usage Principal"],
      [
        ["Amazon EC2", "Machines virtuelles configurables (CPU, RAM, stockage, reseau). Centaines de types d'instances optimisees."],
        ["AWS Lambda", "Execution de code sans serveur. Supporte Java, Python, Node.js, Go, .NET. Timeout max 15 minutes."],
        ["Amazon ECS", "Orchestration de conteneurs Docker sur AWS. Peut utiliser EC2 ou Fargate (serverless)."],
        ["Amazon EKS", "Kubernetes manage sur AWS. Ideal pour les architectures complexes de microservices."],
        ["AWS Fargate", "Moteur serverless pour ECS et EKS. Plus de gestion de serveurs pour vos conteneurs."],
        ["AWS Elastic Beanstalk", "PaaS pour deployer des applications web Java, Node.js, Python, Ruby, PHP, .NET."],
      ]
    ),
    spacer(),
    h3("1.8.2 Services de Stockage"),
    twoColTable(
      ["Service", "Usage Principal"],
      [
        ["Amazon S3", "Stockage d'objets illimite. 99.999999999% de durabilite. Ideal pour fichiers, backups, contenu statique."],
        ["Amazon EBS", "Volumes de stockage bloc attaches aux instances EC2. Comme un disque dur virtuel."],
        ["Amazon EFS", "Systeme de fichiers partage NFS accessible par plusieurs EC2 simultanement."],
        ["Amazon S3 Glacier", "Archivage long terme a cout tres reduit. Recuperation en minutes a heures."],
        ["AWS Storage Gateway", "Connexion entre stockage on-premise et AWS S3/Glacier."],
      ]
    ),
    spacer(),
    h3("1.8.3 Services de Base de Donnees"),
    twoColTable(
      ["Service", "Usage Principal"],
      [
        ["Amazon RDS", "Bases relationnelles managees: MySQL, PostgreSQL, MariaDB, Oracle, SQL Server."],
        ["Amazon Aurora", "Base relationnelle AWS haute performance. Compatible MySQL/PostgreSQL. 5x plus rapide que MySQL standard."],
        ["Amazon DynamoDB", "Base NoSQL serverless a performance previsible en millisecondes. Scaling automatique."],
        ["Amazon ElastiCache", "Cache en memoire: Redis ou Memcached. Reduit la charge sur la base de donnees."],
        ["Amazon Redshift", "Data warehouse pour l'analyse de grandes volumetries de donnees (Big Data)."],
        ["Amazon DocumentDB", "Base documentaire compatible MongoDB."],
      ]
    ),
    spacer(),
    h3("1.8.4 Services Reseau"),
    twoColTable(
      ["Service", "Usage Principal"],
      [
        ["Amazon VPC", "Reseau virtuel prive isole dans AWS. Controle total sur l'adressage IP et le routage."],
        ["Amazon Route 53", "DNS hautement disponible avec routage intelligent (latency, geolocation, failover)."],
        ["Amazon CloudFront", "CDN global avec 450+ points de presence. Cache et distribution de contenu."],
        ["AWS ALB/NLB", "Load balancers pour distribuer le trafic. ALB pour HTTP/HTTPS, NLB pour TCP/UDP."],
        ["AWS API Gateway", "Gestion complete des APIs REST, HTTP et WebSocket."],
        ["AWS Direct Connect", "Connexion reseau privee dedicee entre on-premise et AWS."],
      ]
    ),
    spacer(),

    h2("1.9 Lab : Concevoir une Architecture AWS"),
    infoBox("Objectif du Lab",
      "Dans ce lab, vous allez concevoir sur papier l'architecture AWS d'une application de reservation en ligne. Vous devrez identifier les services necessaires pour chaque couche et justifier vos choix.",
      LIGHT_BLUE, BLUE),
    spacer(),
    h3("Scenario"),
    p("Vous devez concevoir une application de reservation de voyages avec les caracteristiques suivantes :"),
    bullet("100 000 utilisateurs actifs par mois"),
    bullet("Pics de trafic saisonniers (x10 en ete)"),
    bullet("Donnees de reservations (SQL) et catalogue produits (hautes performances)"),
    bullet("Upload de photos de voyages par les utilisateurs"),
    bullet("API mobile (iOS/Android) et site web"),
    bullet("Conformite RGPD (donnees en Europe)"),
    spacer(),
    h3("Solution Proposee"),
    numbered("Region : eu-west-1 (Irlande) pour la conformite RGPD"),
    numbered("DNS : Route 53 avec enregistrements A/AAAA et health checks"),
    numbered("CDN : CloudFront pour le contenu statique (JS, CSS, images)"),
    numbered("Frontend : S3 + CloudFront pour le site React/Angular SPA"),
    numbered("API : API Gateway + Lambda pour les endpoints REST"),
    numbered("Authentification : Amazon Cognito pour users pool"),
    numbered("Base donnees reservations : Aurora PostgreSQL Multi-AZ"),
    numbered("Catalogue produits : DynamoDB (lectures tres frequentes)"),
    numbered("Photos utilisateurs : S3 avec presigned URLs"),
    numbered("Cache : ElastiCache Redis pour les sessions et resultats frequents"),
    numbered("Monitoring : CloudWatch + X-Ray pour l'observabilite"),
    spacer(),

    h2("1.10 Questions de Revision - Module 1"),
    infoBox("Question 1",
      "Une application web doit etre deployee avec une haute disponibilite. Le client exige qu'une panne d'un datacenter entier n'affecte pas le service. Quelle strategie AWS implementez-vous ?\nA) Deployer dans plusieurs regions\nB) Deployer dans plusieurs zones de disponibilite (AZs) dans une meme region\nC) Utiliser un seul EC2 avec plus de RAM\nD) Activer Auto Scaling sur une seule AZ\nReponse correcte : B - Les AZs sont des datacenters physiquement separes. Multi-AZ dans une region protege contre une panne de datacenter.",
      LIGHT_ORANGE, ORANGE),
    spacer(),
    infoBox("Question 2",
      "Dans le modele de responsabilite partagee, qui est responsable du patching du systeme d'exploitation d'une instance Amazon RDS ?\nA) Le client\nB) AWS\nC) Les deux de facon partagee\nD) Le fournisseur de la base de donnees\nReponse correcte : B - RDS est un service manage. AWS gere l'OS, les patches et l'infrastructure. Le client gere la base de donnees (schema, donnees, parametres).",
      LIGHT_ORANGE, ORANGE),
    spacer(),
    infoBox("Question 3",
      "Quel service AWS vous permet de creer un reseau virtuel prive isole avec un controle total sur l'adressage IP ?\nA) Amazon CloudFront\nB) AWS Direct Connect\nC) Amazon VPC\nD) Amazon Route 53\nReponse correcte : C - VPC (Virtual Private Cloud) est le service de reseau virtuel d'AWS qui vous permet de definir votre topologie reseau, CIDR blocks, subnets, tables de routage.",
      LIGHT_ORANGE, ORANGE),
    spacer(),

    h2("1.11 Resume du Module"),
    p("Dans ce module, vous avez appris :"),
    bullet("L'architecture globale d'une application web sur AWS avec ses differentes couches"),
    bullet("Le modele de responsabilite partagee entre AWS et le client"),
    bullet("L'importance des regions et zones de disponibilite pour la haute disponibilite"),
    bullet("Les 6 piliers du Well-Architected Framework"),
    bullet("Les services AWS fondamentaux par categorie (compute, stockage, base de donnees, reseau)"),
    bullet("Comment choisir entre IaaS, PaaS et FaaS selon les besoins"),
    spacer(),
  ];
}

// ============================================================
// MODULE 2: Premiers Pas avec le Developpement AWS
// ============================================================
function module2() {
  return [
    pageBreak(),
    h1("MODULE 2 : Premiers Pas avec le Developpement sur AWS"),
    spacer(),
    p("Ce module vous introduit aux outils fondamentaux du developpeur AWS : le SDK AWS pour Java, l'AWS CLI, et l'environnement de developpement integre AWS Cloud9. Vous apprendrez a configurer votre environnement de developpement et a interagir avec les services AWS par programmation.", { size: 22 }),
    spacer(),

    h2("2.1 Objectifs d'Apprentissage"),
    bullet("Comprendre les differentes methodes d'acces aux services AWS"),
    bullet("Configurer et utiliser l'AWS CLI pour interagir avec les services"),
    bullet("Integrer l'AWS SDK for Java v2 dans vos projets Maven/Gradle"),
    bullet("Comprendre les patterns de programmation avec le SDK AWS"),
    bullet("Utiliser AWS Cloud9 comme environnement de developpement cloud"),
    spacer(),

    h2("2.2 Methodes d'Acces aux Services AWS"),
    p("AWS propose quatre methodes principales pour interagir avec ses services :"),
    spacer(),
    multiColTable(
      ["Methode", "Description", "Cas d'Usage", "Exemple"],
      [1800, 2800, 2360, 2400],
      [
        ["Console Web", "Interface graphique dans le navigateur", "Configuration initiale, exploration, debugging", "console.aws.amazon.com"],
        ["AWS CLI", "Outil ligne de commande", "Automation, scripts, CI/CD pipelines", "aws s3 ls"],
        ["AWS SDK", "Bibliotheques pour langages de programmation", "Applications, microservices, Lambda functions", "S3Client.create()"],
        ["API REST", "Appels HTTP directs aux endpoints AWS", "Langages non supportes par SDK, integration custom", "curl + Signature V4"],
      ]
    ),
    spacer(),

    h2("2.3 AWS CLI - Configuration et Utilisation"),
    h3("2.3.1 Installation de l'AWS CLI v2"),
    p("L'AWS CLI v2 est disponible pour Windows, macOS et Linux. Voici les commandes d'installation :"),
    spacer(),
    codeBlock([
      "# macOS (avec Homebrew)",
      "brew install awscli",
      "",
      "# Verification de l'installation",
      "aws --version",
      "# aws-cli/2.x.x Python/3.x.x ...",
      "",
      "# Linux (x86_64)",
      "curl 'https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' -o 'awscliv2.zip'",
      "unzip awscliv2.zip",
      "sudo ./aws/install",
      "",
      "# Windows (via MSI installer ou winget)",
      "winget install -e --id Amazon.AWSCLI",
    ]),
    spacer(),
    h3("2.3.2 Configuration de l'AWS CLI"),
    p("Apres l'installation, configurez l'AWS CLI avec vos credentials IAM :"),
    spacer(),
    codeBlock([
      "# Configuration interactive (recommandee pour le developpement)",
      "aws configure",
      "",
      "# Le CLI vous demandera :",
      "AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE",
      "AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
      "Default region name [None]: eu-west-1",
      "Default output format [None]: json",
      "",
      "# Les credentials sont stockes dans :",
      "# Linux/Mac : ~/.aws/credentials",
      "# Windows : C:\\Users\\USERNAME\\.aws\\credentials",
      "",
      "# Fichier ~/.aws/credentials",
      "[default]",
      "aws_access_key_id = AKIAIOSFODNN7EXAMPLE",
      "aws_secret_access_key = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
      "",
      "[production]",
      "aws_access_key_id = AKIAI44QH8DHBEXAMPLE",
      "aws_secret_access_key = je7MtGbClwBF/2Zp9Utk/h3yCo8nvbEXAMPLEKEY",
      "",
      "# Fichier ~/.aws/config",
      "[default]",
      "region = eu-west-1",
      "output = json",
      "",
      "[profile production]",
      "region = us-east-1",
      "output = table",
    ]),
    spacer(),
    infoBox("Securite Importante",
      "Ne jamais committer vos credentials AWS dans un depot Git ! Utilisez .gitignore pour exclure le fichier ~/.aws/credentials. En production, utilisez des roles IAM attaches aux instances EC2 ou fonctions Lambda plutot que des Access Keys statiques.",
      LIGHT_RED, "C62828"),
    spacer(),
    h3("2.3.3 Commandes AWS CLI Essentielles"),
    p("Voici les commandes CLI les plus utilisees par les developpeurs Java :"),
    spacer(),
    codeBlock([
      "# === GESTION DES PROFILS ===",
      "aws configure list                           # Lister la configuration actuelle",
      "aws configure list-profiles                  # Lister les profils disponibles",
      "aws sts get-caller-identity                  # Verifier quel compte/role est utilise",
      "",
      "# === AMAZON S3 ===",
      "aws s3 ls                                    # Lister tous les buckets",
      "aws s3 ls s3://mon-bucket/                   # Lister le contenu d'un bucket",
      "aws s3 cp fichier.txt s3://mon-bucket/       # Copier un fichier vers S3",
      "aws s3 sync ./dist s3://mon-bucket/static/   # Synchroniser un dossier avec S3",
      "aws s3 mb s3://nouveau-bucket --region eu-west-1  # Creer un bucket",
      "aws s3 presign s3://bucket/fichier.pdf --expires-in 3600  # URL preSignee (1h)",
      "",
      "# === AWS LAMBDA ===",
      "aws lambda list-functions                    # Lister toutes les fonctions",
      "aws lambda invoke --function-name ma-fonction \\",
      "  --payload '{\"key\":\"value\"}' output.json   # Invoquer une fonction",
      "aws lambda update-function-code \\",
      "  --function-name ma-fonction \\",
      "  --zip-file fileb://function.zip            # Deployer une fonction",
      "",
      "# === AMAZON DYNAMODB ===",
      "aws dynamodb list-tables                     # Lister les tables",
      "aws dynamodb describe-table --table-name ma-table  # Decrire une table",
      "aws dynamodb scan --table-name ma-table      # Scanner tous les items",
      "",
      "# === AMAZON EC2 ===",
      "aws ec2 describe-instances                   # Lister les instances",
      "aws ec2 start-instances --instance-ids i-1234567890abcdef0",
      "aws ec2 stop-instances --instance-ids i-1234567890abcdef0",
      "",
      "# === FORMAT DE SORTIE ===",
      "aws s3 ls --output table                     # Format tableau",
      "aws s3 ls --output yaml                      # Format YAML",
      "aws s3 ls --query 'Buckets[].Name'           # Filtrer avec JMESPath",
    ]),
    spacer(),

    h2("2.4 AWS SDK for Java v2 - Configuration Maven"),
    p("L'AWS SDK for Java v2 est une refonte complete du SDK v1, avec une API asynchrone, des clients immuables et une meilleure integration avec les frameworks modernes."),
    spacer(),
    h3("2.4.1 Configuration du pom.xml avec BOM"),
    p("La methode recommandee est d'utiliser le BOM (Bill of Materials) AWS qui gere les versions de tous les modules :"),
    spacer(),
    codeBlock([
      "<!-- pom.xml -->",
      "<project>",
      "  <properties>",
      "    <java.version>17</java.version>",
      "    <aws.java.sdk.version>2.25.0</aws.java.sdk.version>",
      "  </properties>",
      "",
      "  <dependencyManagement>",
      "    <dependencies>",
      "      <!-- BOM AWS SDK - gere toutes les versions -->",
      "      <dependency>",
      "        <groupId>software.amazon.awssdk</groupId>",
      "        <artifactId>bom</artifactId>",
      "        <version>${aws.java.sdk.version}</version>",
      "        <type>pom</type>",
      "        <scope>import</scope>",
      "      </dependency>",
      "    </dependencies>",
      "  </dependencyManagement>",
      "",
      "  <dependencies>",
      "    <!-- Services AWS (pas besoin de specifier la version avec BOM) -->",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>s3</artifactId>",
      "    </dependency>",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>dynamodb</artifactId>",
      "    </dependency>",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>lambda</artifactId>",
      "    </dependency>",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>sqs</artifactId>",
      "    </dependency>",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>sns</artifactId>",
      "    </dependency>",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>cognitoidentityprovider</artifactId>",
      "    </dependency>",
      "    <!-- Client HTTP (Netty pour async, Apache pour sync) -->",
      "    <dependency>",
      "      <groupId>software.amazon.awssdk</groupId>",
      "      <artifactId>netty-nio-client</artifactId>",
      "    </dependency>",
      "    <!-- Logging -->",
      "    <dependency>",
      "      <groupId>org.slf4j</groupId>",
      "      <artifactId>slf4j-simple</artifactId>",
      "      <version>2.0.9</version>",
      "    </dependency>",
      "  </dependencies>",
      "</project>",
    ]),
    spacer(),
    h3("2.4.2 Chaine de Credentials AWS SDK v2"),
    p("Le SDK AWS v2 utilise une chaine de providers pour trouver les credentials. L'ordre de resolution est :"),
    spacer(),
    codeBlock([
      "// Ordre de resolution des credentials (DefaultCredentialsProvider)",
      "//",
      "// 1. Variables d'environnement",
      "//    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN",
      "//",
      "// 2. Proprietes systeme Java",
      "//    aws.accessKeyId, aws.secretAccessKey",
      "//",
      "// 3. Fichier de credentials AWS (~/.aws/credentials)",
      "//    Profile par defaut ou AWS_PROFILE env variable",
      "//",
      "// 4. AWS SSO (Single Sign-On)",
      "//",
      "// 5. Metadata du conteneur ECS (EcsContainerCredentialsProvider)",
      "//",
      "// 6. Metadata de l'instance EC2/Lambda (InstanceProfileCredentialsProvider)",
      "//    http://169.254.169.254/latest/meta-data/iam/security-credentials/",
      "",
      "import software.amazon.awssdk.auth.credentials.*;",
      "import software.amazon.awssdk.regions.Region;",
      "import software.amazon.awssdk.services.s3.S3Client;",
      "",
      "public class CredentialsExamples {",
      "",
      "    // Methode 1 : Provider par defaut (recommande en production)",
      "    public static S3Client defaultCredentials() {",
      "        return S3Client.builder()",
      "            .region(Region.EU_WEST_1)",
      "            .credentialsProvider(DefaultCredentialsProvider.create())",
      "            .build();",
      "    }",
      "",
      "    // Methode 2 : Credentials statiques (dev seulement, jamais en prod !)",
      "    public static S3Client staticCredentials() {",
      "        return S3Client.builder()",
      "            .region(Region.EU_WEST_1)",
      "            .credentialsProvider(StaticCredentialsProvider.create(",
      "                AwsBasicCredentials.create(",
      "                    'AKIAIOSFODNN7EXAMPLE',",
      "                    'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'",
      "                )",
      "            ))",
      "            .build();",
      "    }",
      "",
      "    // Methode 3 : Profil specifique",
      "    public static S3Client profileCredentials() {",
      "        return S3Client.builder()",
      "            .region(Region.EU_WEST_1)",
      "            .credentialsProvider(",
      "                ProfileCredentialsProvider.create('production')",
      "            )",
      "            .build();",
      "    }",
      "",
      "    // Methode 4 : Role IAM assume (AssumeRole cross-account)",
      "    public static S3Client assumeRoleCredentials() {",
      "        StsClient stsClient = StsClient.create();",
      "        return S3Client.builder()",
      "            .region(Region.EU_WEST_1)",
      "            .credentialsProvider(",
      "                StsAssumeRoleCredentialsProvider.builder()",
      "                    .stsClient(stsClient)",
      "                    .refreshRequest(r -> r",
      "                        .roleArn('arn:aws:iam::123456789012:role/MonRole')",
      "                        .roleSessionName('my-session')",
      "                    )",
      "                    .build()",
      "            )",
      "            .build();",
      "    }",
      "}",
    ]),
    spacer(),

    h2("2.5 Patterns de Programmation avec le SDK AWS v2"),
    h3("2.5.1 Pattern Builder"),
    p("Tous les objets du SDK AWS v2 utilisent le pattern Builder pour la creation immuable :"),
    spacer(),
    codeBlock([
      "import software.amazon.awssdk.services.s3.S3Client;",
      "import software.amazon.awssdk.services.s3.model.*;",
      "import software.amazon.awssdk.regions.Region;",
      "",
      "public class BuilderPatternExample {",
      "",
      "    public static void main(String[] args) {",
      "        // 1. Creation du client (objet lourd, creer une seule fois et reutiliser)",
      "        S3Client s3 = S3Client.builder()",
      "            .region(Region.EU_WEST_1)",
      "            .build();",
      "",
      "        // 2. Construction de la requete avec Builder",
      "        ListObjectsV2Request request = ListObjectsV2Request.builder()",
      "            .bucket('mon-bucket')",
      "            .prefix('images/')",
      "            .maxKeys(100)",
      "            .build();",
      "",
      "        // 3. Execution et recuperation de la reponse",
      "        ListObjectsV2Response response = s3.listObjectsV2(request);",
      "",
      "        // 4. Traitement de la reponse",
      "        response.contents().forEach(obj -> {",
      "            System.out.println('Cle: ' + obj.key());",
      "            System.out.println('Taille: ' + obj.size() + ' bytes');",
      "            System.out.println('Modifie: ' + obj.lastModified());",
      "        });",
      "",
      "        // 5. Fermeture du client (libere les connexions reseau)",
      "        s3.close();",
      "    }",
      "}",
    ]),
    spacer(),
    h3("2.5.2 Gestion des Exceptions AWS"),
    p("Le SDK AWS v2 utilise une hierarchie d'exceptions specifique. Comprendre cette hierarchie est essentiel pour gerer correctement les erreurs :"),
    spacer(),
    codeBlock([
      "import software.amazon.awssdk.core.exception.SdkClientException;",
      "import software.amazon.awssdk.services.s3.model.S3Exception;",
      "import software.amazon.awssdk.services.s3.model.NoSuchBucketException;",
      "import software.amazon.awssdk.services.s3.model.NoSuchKeyException;",
      "",
      "public class ExceptionHandlingExample {",
      "",
      "    public void downloadFile(S3Client s3, String bucket, String key) {",
      "        try {",
      "            GetObjectRequest request = GetObjectRequest.builder()",
      "                .bucket(bucket)",
      "                .key(key)",
      "                .build();",
      "",
      "            ResponseBytes<GetObjectResponse> objectBytes = ",
      "                s3.getObjectAsBytes(request);",
      "            byte[] data = objectBytes.asByteArray();",
      "            System.out.println('Fichier telecharge: ' + data.length + ' bytes');",
      "",
      "        } catch (NoSuchKeyException e) {",
      "            // Objet specifique non trouve dans le bucket",
      "            System.err.println('Fichier non trouve: ' + key);",
      "            System.err.println('Code erreur: ' + e.statusCode()); // 404",
      "        } catch (NoSuchBucketException e) {",
      "            // Bucket inexistant",
      "            System.err.println('Bucket non trouve: ' + bucket);",
      "        } catch (S3Exception e) {",
      "            // Erreur S3 generique (permissions, rate limiting, etc.)",
      "            System.err.println('Erreur S3: ' + e.getMessage());",
      "            System.err.println('Status HTTP: ' + e.statusCode());",
      "            System.err.println('Code AWS: ' + e.awsErrorDetails().errorCode());",
      "            System.err.println('Message: ' + e.awsErrorDetails().errorMessage());",
      "        } catch (SdkClientException e) {",
      "            // Erreur cote client (pas d'internet, timeout, etc.)",
      "            System.err.println('Erreur client SDK: ' + e.getMessage());",
      "        }",
      "    }",
      "}",
    ]),
    spacer(),
    h3("2.5.3 Pattern Paginator pour les Resultats Volumineux"),
    p("De nombreux services AWS retournent des resultats pagines. Le SDK v2 propose des iterateurs automatiques appeles Paginators :"),
    spacer(),
    codeBlock([
      "import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;",
      "",
      "public class PaginatorExample {",
      "",
      "    // Sans paginator (vous devez gerer la pagination manuellement)",
      "    public void listAllObjectsManual(S3Client s3, String bucket) {",
      "        String continuationToken = null;",
      "        do {",
      "            ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()",
      "                .bucket(bucket)",
      "                .maxKeys(1000);",
      "            if (continuationToken != null) {",
      "                builder.continuationToken(continuationToken);",
      "            }",
      "            ListObjectsV2Response response = s3.listObjectsV2(builder.build());",
      "            response.contents().forEach(obj -> process(obj));",
      "            continuationToken = response.isTruncated() ? ",
      "                response.nextContinuationToken() : null;",
      "        } while (continuationToken != null);",
      "    }",
      "",
      "    // AVEC paginator (recommande - gestion automatique de la pagination)",
      "    public void listAllObjectsWithPaginator(S3Client s3, String bucket) {",
      "        ListObjectsV2Request request = ListObjectsV2Request.builder()",
      "            .bucket(bucket)",
      "            .build();",
      "",
      "        // Le paginator gere automatiquement toutes les pages",
      "        ListObjectsV2Iterable paginator = s3.listObjectsV2Paginator(request);",
      "",
      "        // Iteration sur tous les objets (toutes pages confondues)",
      "        paginator.contents().forEach(obj -> process(obj));",
      "",
      "        // Ou iteration page par page",
      "        paginator.forEach(page -> {",
      "            System.out.println('Page avec ' + page.keyCount() + ' objets');",
      "            page.contents().forEach(obj -> process(obj));",
      "        });",
      "    }",
      "",
      "    private void process(S3Object obj) {",
      "        System.out.println(obj.key() + ' - ' + obj.size());",
      "    }",
      "}",
    ]),
    spacer(),

    h2("2.6 AWS Cloud9 - Environnement de Developpement Cloud"),
    p("AWS Cloud9 est un IDE (Integrated Development Environment) base sur le navigateur, heberge dans le cloud. Il vous donne un acces direct a un terminal Linux avec l'AWS CLI pre-configure."),
    spacer(),
    h3("2.6.1 Avantages de Cloud9"),
    bullet("Acces depuis n'importe quel navigateur, sans installation locale"),
    bullet("AWS CLI et SDK pre-installes et configures avec le role IAM de l'environnement"),
    bullet("Pas de gestion de credentials : utilise le role IAM de l'instance EC2 sous-jacente"),
    bullet("Partage de code en temps reel pour la revue de code ou le pair programming"),
    bullet("Pre-configure avec Java, Python, Node.js, Ruby, PHP"),
    bullet("Terminal Linux integre avec acces direct aux services AWS"),
    spacer(),
    h3("2.6.2 Creer un Environnement Cloud9"),
    codeBlock([
      "# Creer un environnement Cloud9 via AWS CLI",
      "aws cloud9 create-environment-ec2 \\",
      "  --name 'MonEnvironnementDev' \\",
      "  --description 'Environnement de developpement Java AWS' \\",
      "  --instance-type t3.small \\",
      "  --image-id ami-01234567890abcdef \\",
      "  --region eu-west-1",
      "",
      "# L'environnement est accessible via :",
      "# https://eu-west-1.console.aws.amazon.com/cloud9",
      "",
      "# Dans Cloud9, verifier la configuration AWS",
      "aws sts get-caller-identity",
      "# {",
      "#   'UserId': 'AROAIOSFODNN7EXAMPLE:i-1234567890abcdef0',",
      "#   'Account': '123456789012',",
      "#   'Arn': 'arn:aws:sts::123456789012:assumed-role/cloud9-role/i-123...'",
      "# }",
    ]),
    spacer(),

    h2("2.7 Configuration d'un Projet Java AWS dans IntelliJ IDEA"),
    p("Pour developper avec AWS SDK en dehors de Cloud9, voici la configuration recommandee pour IntelliJ IDEA :"),
    spacer(),
    codeBlock([
      "# 1. Installer le plugin AWS Toolkit dans IntelliJ",
      "# File > Settings > Plugins > Marketplace > 'AWS Toolkit'",
      "",
      "# 2. Configurer les credentials dans IntelliJ",
      "# File > Settings > Tools > AWS > Connection settings",
      "# Selectionnez 'Use configured profile' et choisissez votre profil",
      "",
      "# 3. Structure de projet recommandee",
      "mon-projet-aws/",
      "├── src/",
      "│   ├── main/",
      "│   │   ├── java/",
      "│   │   │   └── com/monapp/",
      "│   │   │       ├── config/         # Configuration AWS clients",
      "│   │   │       ├── service/        # Logique metier",
      "│   │   │       ├── model/          # Modeles de donnees",
      "│   │   │       └── Main.java",
      "│   │   └── resources/",
      "│   │       └── application.properties",
      "│   └── test/",
      "│       └── java/",
      "│           └── com/monapp/",
      "├── pom.xml",
      "└── .gitignore    # Ne pas oublier d'exclure ~/.aws/credentials !",
      "",
      "# 4. Contenu du .gitignore",
      "# .aws/",
      "# *.credentials",
      "# target/",
      "# .idea/",
    ]),
    spacer(),
    h3("2.7.1 Configuration des Clients AWS (Singleton Pattern)"),
    p("Les clients AWS sont des objets lourds qui maintiennent des pools de connexions. Ils doivent etre crees une seule fois et reutilises :"),
    spacer(),
    codeBlock([
      "package com.monapp.config;",
      "",
      "import software.amazon.awssdk.regions.Region;",
      "import software.amazon.awssdk.services.s3.S3Client;",
      "import software.amazon.awssdk.services.dynamodb.DynamoDbClient;",
      "import software.amazon.awssdk.services.lambda.LambdaClient;",
      "import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;",
      "import software.amazon.awssdk.http.apache.ApacheHttpClient;",
      "import java.time.Duration;",
      "",
      "// Configuration centralisee des clients AWS (Singleton thread-safe)",
      "public class AwsClientConfig {",
      "",
      "    private static final Region REGION = Region.EU_WEST_1;",
      "    private static volatile S3Client s3Client;",
      "    private static volatile DynamoDbClient dynamoDbClient;",
      "",
      "    // Double-checked locking pour thread safety",
      "    public static S3Client getS3Client() {",
      "        if (s3Client == null) {",
      "            synchronized (AwsClientConfig.class) {",
      "                if (s3Client == null) {",
      "                    s3Client = S3Client.builder()",
      "                        .region(REGION)",
      "                        .credentialsProvider(DefaultCredentialsProvider.create())",
      "                        .httpClientBuilder(ApacheHttpClient.builder()",
      "                            .connectionTimeout(Duration.ofSeconds(5))",
      "                            .socketTimeout(Duration.ofSeconds(30))",
      "                            .maxConnections(50)",
      "                        )",
      "                        .build();",
      "                }",
      "            }",
      "        }",
      "        return s3Client;",
      "    }",
      "",
      "    public static DynamoDbClient getDynamoDbClient() {",
      "        if (dynamoDbClient == null) {",
      "            synchronized (AwsClientConfig.class) {",
      "                if (dynamoDbClient == null) {",
      "                    dynamoDbClient = DynamoDbClient.builder()",
      "                        .region(REGION)",
      "                        .build();",
      "                }",
      "            }",
      "        }",
      "        return dynamoDbClient;",
      "    }",
      "}",
    ]),
    spacer(),

    h2("2.8 Variables d'Environnement et Configuration"),
    p("La bonne pratique est d'externaliser la configuration pour ne pas coder en dur les valeurs specifiques a l'environnement :"),
    spacer(),
    codeBlock([
      "package com.monapp.config;",
      "",
      "// Gestionnaire de configuration via variables d'environnement",
      "public class AppConfig {",
      "",
      "    // Lire depuis les variables d'environnement avec valeur par defaut",
      "    public static String getBucketName() {",
      "        return System.getenv().getOrDefault('S3_BUCKET_NAME', 'dev-bucket');",
      "    }",
      "",
      "    public static String getDynamoTableName() {",
      "        return System.getenv().getOrDefault('DYNAMO_TABLE', 'dev-reservations');",
      "    }",
      "",
      "    public static String getRegion() {",
      "        return System.getenv().getOrDefault('AWS_REGION', 'eu-west-1');",
      "    }",
      "",
      "    // Variables d'environnement pour les environnements :",
      "    // DEV  : export S3_BUCKET_NAME=dev-bucket AWS_REGION=eu-west-1",
      "    // PROD : S3_BUCKET_NAME=prod-bucket AWS_REGION=eu-west-1 (dans ECS/Lambda)",
      "}",
    ]),
    spacer(),

    h2("2.9 Lab : Configurer l'Environnement de Developpement"),
    infoBox("Objectif du Lab",
      "Dans ce lab, vous allez configurer completement votre environnement de developpement AWS, verifier les permissions et executer vos premieres commandes AWS CLI et SDK Java.",
      LIGHT_BLUE, BLUE),
    spacer(),
    h3("Etape 1 : Installer et Configurer l'AWS CLI"),
    numbered("Installer AWS CLI v2 selon votre OS (voir section 2.3.1)"),
    numbered("Creer un utilisateur IAM avec MFA dans la console AWS"),
    numbered("Generer des Access Keys pour cet utilisateur"),
    numbered("Executer 'aws configure' et saisir vos credentials"),
    numbered("Verifier : 'aws sts get-caller-identity'"),
    spacer(),
    h3("Etape 2 : Tester les Permissions S3"),
    codeBlock([
      "# Lister les buckets (permission s3:ListAllMyBuckets requise)",
      "aws s3 ls",
      "",
      "# Creer un bucket de test (remplacer avec votre nom unique)",
      "aws s3 mb s3://mon-test-bucket-$(date +%s) --region eu-west-1",
      "",
      "# Creer et uploader un fichier",
      "echo 'Hello AWS!' > test.txt",
      "aws s3 cp test.txt s3://mon-test-bucket-XXX/test.txt",
      "",
      "# Verifier l'upload",
      "aws s3 ls s3://mon-test-bucket-XXX/",
      "",
      "# Supprimer le bucket de test",
      "aws s3 rm s3://mon-test-bucket-XXX/test.txt",
      "aws s3 rb s3://mon-test-bucket-XXX",
    ]),
    spacer(),
    h3("Etape 3 : Premier Programme Java avec AWS SDK"),
    codeBlock([
      "package com.monapp;",
      "",
      "import software.amazon.awssdk.regions.Region;",
      "import software.amazon.awssdk.services.s3.S3Client;",
      "import software.amazon.awssdk.services.s3.model.ListBucketsResponse;",
      "",
      "public class HelloAWS {",
      "    public static void main(String[] args) {",
      "        // Creer le client S3",
      "        try (S3Client s3 = S3Client.builder()",
      "                .region(Region.EU_WEST_1)",
      "                .build()) {",
      "",
      "            // Lister les buckets",
      "            ListBucketsResponse response = s3.listBuckets();",
      "",
      "            System.out.println('=== Mes Buckets S3 ===');",
      "            response.buckets().forEach(bucket -> {",
      "                System.out.println('  - ' + bucket.name() + ",
      "                    ' (cree le: ' + bucket.creationDate() + ')');",
      "            });",
      "            System.out.println('Total: ' + response.buckets().size() + ' buckets');",
      "        }",
      "    }",
      "}",
    ]),
    spacer(),

    h2("2.10 Questions de Revision - Module 2"),
    infoBox("Question 1",
      "Dans quelle ordre le DefaultCredentialsProvider du SDK AWS Java v2 recherche-t-il les credentials ?\nA) Fichier credentials > Variables d'environnement > Instance metadata\nB) Variables d'environnement > Proprietes systeme > Fichier credentials > Instance metadata\nC) Instance metadata > Variables d'environnement > Fichier credentials\nD) Fichier credentials > Instance metadata > Variables d'environnement\nReponse correcte : B - L'ordre est : variables d'environnement, proprietes systeme Java, profil SSO, fichier credentials, container credentials, puis instance metadata EC2/Lambda.",
      LIGHT_ORANGE, ORANGE),
    spacer(),
    infoBox("Question 2",
      "Vous developpez une application Java qui doit interagir avec DynamoDB depuis une instance EC2. Quelle est la meilleure pratique pour gerer les credentials AWS ?\nA) Stocker les Access Keys dans le code source Java\nB) Mettre les Access Keys dans un fichier .env sur l'instance\nC) Attacher un role IAM a l'instance EC2 avec les permissions necessaires\nD) Configurer aws configure sur chaque instance\nReponse correcte : C - Les roles IAM attaches aux instances EC2 sont la meilleure pratique. Les credentials temporaires sont automatiquement injectes et renouvelés. Aucune gestion manuelle de credentials statiques.",
      LIGHT_ORANGE, ORANGE),
    spacer(),

    h2("2.11 Resume du Module"),
    bullet("L'AWS CLI permet d'interagir avec tous les services AWS depuis la ligne de commande"),
    bullet("Le SDK AWS Java v2 utilise le pattern Builder pour creer des requetes immutables"),
    bullet("DefaultCredentialsProvider resout automatiquement les credentials selon un ordre de priorite"),
    bullet("Les clients AWS doivent etre crees une seule fois (Singleton) pour les performances"),
    bullet("AWS Cloud9 offre un IDE cloud pre-configure avec l'AWS CLI et les SDK"),
    bullet("Externalisez toujours la configuration (region, noms de ressources) via variables d'environnement"),
    spacer(),
  ];
}

// ============================================================
// GENERATION DU DOCUMENT
// ============================================================
async function generateDocument() {
  console.log('Debut de la generation du cours AWS...');

  const content = [
    // Page de titre
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 2880, after: 480 },
      children: [new TextRun({ text: "COURS COMPLET", font: "Arial", size: 48, bold: true, color: BLUE })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 480 },
      children: [new TextRun({ text: "Developpement AWS avec Java", font: "Arial", size: 56, bold: true, color: ORANGE })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 240 },
      children: [new TextRun({ text: "Preparation a la Certification", font: "Arial", size: 32, color: DARK_GRAY })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 240 },
      children: [new TextRun({ text: "AWS Certified Developer - Associate (DVA-C02)", font: "Arial", size: 36, bold: true, color: BLUE })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 480 },
      children: [new TextRun({ text: "AWS Certified Solutions Architect - Associate (SAA-C03)", font: "Arial", size: 28, color: DARK_GRAY })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 960 },
      border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: ORANGE } },
      children: [new TextRun({ text: " ", size: 24 })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 120 },
      children: [new TextRun({ text: "Couverture Complete des Modules :", font: "Arial", size: 24, bold: true })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 480 },
      children: [new TextRun({ text: "S3 | DynamoDB | Lambda | API Gateway | Cognito | SAM | CloudWatch | X-Ray", font: "Arial", size: 22, color: DARK_GRAY })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 120 },
      children: [new TextRun({ text: "AWS SDK for Java v2 | AWS CLI | Cloud9 | Step Functions", font: "Arial", size: 22, color: DARK_GRAY })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 960 },
      children: [new TextRun({ text: "Version 2025 - AWS SDK Java v2.25+", font: "Arial", size: 20, color: "666666" })]
    }),

    pageBreak(),

    // Table des matieres
    new Paragraph({
      heading: HeadingLevel.HEADING_1,
      children: [new TextRun({ text: "TABLE DES MATIERES", bold: true, color: BLUE })]
    }),
    new TableOfContents("Table des Matieres", { hyperlink: true, headingStyleRange: "1-3" }),

    ...module1(),
    ...module2(),
  ];

  const doc = new Document({
    numbering: {
      config: [
        {
          reference: "bullets",
          levels: [{
            level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
            style: { paragraph: { indent: { left: 720, hanging: 360 } }, run: { font: "Symbol" } }
          }]
        },
        {
          reference: "numbers",
          levels: [{
            level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
            style: { paragraph: { indent: { left: 720, hanging: 360 } } }
          }]
        },
      ]
    },
    styles: {
      default: {
        document: { run: { font: "Arial", size: 22 } }
      },
      paragraphStyles: [
        {
          id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 40, bold: true, font: "Arial", color: BLUE },
          paragraph: { spacing: { before: 480, after: 240 }, outlineLevel: 0,
            border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: ORANGE } } }
        },
        {
          id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 32, bold: true, font: "Arial", color: BLUE },
          paragraph: { spacing: { before: 320, after: 160 }, outlineLevel: 1 }
        },
        {
          id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 26, bold: true, font: "Arial", color: DARK_GRAY },
          paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 2 }
        },
        {
          id: "Heading4", name: "Heading 4", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 24, bold: true, font: "Arial" },
          paragraph: { spacing: { before: 200, after: 100 }, outlineLevel: 3 }
        },
      ]
    },
    sections: [{
      properties: {
        page: {
          size: { width: 12240, height: 15840 },
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
        }
      },
      headers: {
        default: new Header({
          children: [new Paragraph({
            border: { bottom: { style: BorderStyle.SINGLE, size: 2, color: BLUE } },
            children: [
              new TextRun({ text: "Cours AWS pour Developpeurs Java", font: "Arial", size: 18, color: BLUE }),
              new TextRun({ text: "    |    Certification DVA-C02 & SAA-C03", font: "Arial", size: 18, color: "888888" })
            ]
          })]
        })
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            border: { top: { style: BorderStyle.SINGLE, size: 2, color: BLUE } },
            children: [
              new TextRun({ text: "AWS SDK for Java v2  -  2025  ", font: "Arial", size: 18, color: "888888" }),
              new TextRun({ text: "Page ", font: "Arial", size: 18, color: BLUE }),
              new TextRun({ children: [PageNumber.CURRENT], font: "Arial", size: 18, color: BLUE }),
            ]
          })]
        })
      },
      children: content
    }]
  });

  const buffer = await Packer.toBuffer(doc);
  const outputPath = '/Users/user/Desktop/travel-agency/backend/.claude/worktrees/naughty-jepsen-0d5f95/cours_aws_java_part1.docx';
  fs.writeFileSync(outputPath, buffer);
  console.log('Part 1 generee : ' + outputPath);
  console.log('Taille: ' + Math.round(buffer.length / 1024) + ' KB');
}

generateDocument().catch(console.error);
