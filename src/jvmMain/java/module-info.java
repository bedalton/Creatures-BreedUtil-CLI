module bedalton.creatures.breed.cli {
    requires kotlinx.serialization.json;
    requires kotlin.stdlib;
    requires kotlinx.cli;
    requires korlibs.korim;
    requires kotlinx.coroutines.core;

    requires com.bedalton.creatures.exports.minimal;
    requires com.bedalton.creatures.cli;
    requires com.bedalton.files;
    requires com.bedalton.app;
    requires com.bedalton.common;
    requires com.bedalton.cli;
    requires com.bedalton.creatures.genome;
    requires com.bedalton.creatures.breed.tasks;
    requires com.bedalton.coroutines;
    requires com.bedalton.log;

    exports bedalton.creatures.breed.converter.cli;
}