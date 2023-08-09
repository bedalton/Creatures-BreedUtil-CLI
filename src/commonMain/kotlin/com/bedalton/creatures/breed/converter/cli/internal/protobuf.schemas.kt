package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.app.exitNative
import com.bedalton.app.exitNativeWithError
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.toListOf
import com.bedalton.creatures.creature.common.core.structs.ChemicalAmount
import com.bedalton.creatures.exports.minimal.ExportRenderData
import com.bedalton.creatures.genetics.genome.GeneBytePair
import com.bedalton.creatures.genetics.genome.allGenomeDescriptors
import com.bedalton.log.Log
import com.bedalton.vfs.LocalFileSystem
import com.bedalton.vfs.PathNotAbsoluteException
import com.bedalton.vfs.isFile
import korlibs.io.lang.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator

private const val DEFAULT_PACKAGE = "com.bedalton.creatures.genetics.genome.protobuf"

private val protoDescriptors by lazy {
    listOf(
        // Other
        PrintResults.serializer().descriptor,
        ColorInfo.serializer().descriptor,
        ChemicalAmount.serializer().descriptor,
        ExportRenderData.serializer().descriptor
    ) + allGenomeDescriptors
}

private val geneBytePairsDescriptors by lazy {
    listOf(
        GeneBytePair.serializer().descriptor,
        GeneBytePairs.serializer().descriptor
    )
}


@OptIn(ExperimentalSerializationApi::class)
suspend fun outputAllProtobufSchemas(
    path: String?,
    packageName: String?,
    options: Map<String, String>?,
    includeGeneBytePairs: Boolean
): List<String> {

    if (path != null && !PathUtil.isAbsolute(path)) {
        throw PathNotAbsoluteException("Path to protobuf output must be absolute")
    }
    val isFile = path != null && path.lowercase().endsWith(".proto3")
    val fs = LocalFileSystem
        ?: throw IOException("Failed to get local file system")
    if (path != null && fs.fileExists(path)) {
        if (!isFile && fs.isFile(path)) {
            exitNativeWithError(1, "Schema output directory is actually a file; Path: $path")
        } else if (isFile && fs.isDirectory(path)) {
            exitNativeWithError(1, "Schema output file is actually a directory; Path: $path")
        }
    } else if (path != null) {
        try {
            val directory = if (!isFile) {
                path
            } else {
                PathUtil.getWithoutLastPathComponent(path)
                    ?: exitNativeWithError(1, "Failed to get parent directory for output proto")
            }
            fs.makeDirectory(directory, true)
        } catch (e: Exception) {
            exitNativeWithError(1, "Failed to create directory; ${e.formatted(false)}")
        }
    }

    val descriptors = if (includeGeneBytePairs) {
        allGenomeDescriptors + geneBytePairsDescriptors
    } else {
        allGenomeDescriptors
    }

    if (path == null || isFile) {
        val schemas = generateSchema(descriptors, packageName, options)
        try {
            if (path == null) {
                Log.i { schemas }
                exitNative(0)
            } else {
                fs.write(path, schemas, false)
            }
        } catch (e: Exception) {
            exitNativeWithError(1, "Failed to write schemas to file; ${e.formatted()}")
        }
        return listOf(path)
    }

    return descriptors.map { descriptor ->
        val outputFile = PathUtil.combine(path, descriptor.serialName + ".proto3")
        val schema = generateSchema(descriptor, packageName, options)
        try {
            fs.write(outputFile, schema, false)
        } catch (e: Exception) {
            exitNativeWithError(1, "Failed to write schema ${descriptor.serialName} to file; ${e.formatted()}")
        }
        outputFile
    }
}

private fun generateSchema(descriptor: SerialDescriptor, packageName: String?, options: Map<String, String>?): String {
    return generateSchema(descriptor.toListOf(), packageName, options)
}

private fun generateSchema(
    descriptors: List<SerialDescriptor>,
    packageName: String?,
    options: Map<String, String>?
): String {
    return try {
        ProtoBufSchemaGenerator.generateSchemaText(
            descriptors = descriptors,
            packageName = packageName ?: DEFAULT_PACKAGE,
            options = options ?: mapOf()
        )
    } catch (e: Exception) {
        exitNativeWithError(1, "Failed to generate protobuf schema. ${e.formatted()}")
    }
}