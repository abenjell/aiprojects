const fs = require('fs');
const path = require('path');

// Récupérer le chemin du fichier Java à analyser
const filePath = process.argv[2];

if (!filePath) {
    console.error("Erreur : Veuillez spécifier le chemin d'un fichier Java à analyser.");
    console.error("Usage : node validate_reactive_rules.cjs <path-to-java-file>");
    process.exit(1);
}

const absolutePath = path.resolve(filePath);

if (!fs.existsSync(absolutePath)) {
    console.error(`Erreur : Le fichier n'existe pas : ${filePath}`);
    process.exit(1);
}

try {
    const content = fs.readFileSync(absolutePath, 'utf8');
    const lines = content.split('\n');

    const rules = [
        {
            pattern: /\.block\(/,
            name: "Appel bloquant Reactor (.block())",
            message: "L'appel à .block() sur un Mono ou un Flux est interdit dans un contexte réactif."
        },
        {
            pattern: /Thread\s*\.\s*sleep\s*\(/,
            name: "Sommeil de thread (Thread.sleep())",
            message: "Thread.sleep() bloque le thread de l'Event Loop réactif. Utilisez Mono.delay() ou Flux.interval() à la place."
        },
        {
            pattern: /RestTemplate\b/,
            name: "Utilisation de RestTemplate",
            message: "RestTemplate est synchrone et bloquant. Utilisez WebClient ou RestClient non-bloquant."
        }
    ];

    const violations = [];

    lines.forEach((line, index) => {
        // Ignorer les commentaires simples ou de bloc
        const trimmed = line.trim();
        if (trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*')) {
            return;
        }

        rules.forEach(rule => {
            if (rule.pattern.test(line)) {
                violations.push({
                    lineNum: index + 1,
                    code: trimmed,
                    rule: rule.name,
                    suggestion: rule.message
                });
            }
        });
    });

    if (violations.length > 0) {
        console.log(`❌ Échec de la validation réactive pour : ${path.basename(filePath)}`);
        console.log(`Trouvé ${violations.length} violation(s) de blocage :`);
        violations.forEach(v => {
            console.log(`  - Ligne ${v.lineNum}: [${v.rule}] -> "${v.code}"`);
            console.log(`    💡 Suggestion: ${v.suggestion}`);
        });
        process.exit(1);
    } else {
        console.log(`✅ Validation réussie : Aucune API bloquante détectée dans ${path.basename(filePath)}.`);
        process.exit(0);
    }

} catch (err) {
    console.error(`Erreur lors de la lecture du fichier : ${err.message}`);
    process.exit(1);
}
