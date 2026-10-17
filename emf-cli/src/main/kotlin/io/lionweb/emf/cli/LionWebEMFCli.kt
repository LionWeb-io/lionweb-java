@file:JvmName("LionWebEMFCli")

package io.lionweb.emf.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption

/**
 * Command line interface exposing the Ecore/EMF import and export capabilities of the emf module.
 */
class LionWebEMFCommand : CliktCommand(name = "lionweb-emf") {
    init {
        versionOption(LionWebEMFCommand::class.java.`package`?.implementationVersion ?: "unknown")
    }

    override fun help(context: Context) = "Import and export Ecore metamodels and EMF models from/to LionWeb."

    override fun run() = Unit
}

fun createLionWebEMFCommand(): CliktCommand =
    LionWebEMFCommand().subcommands(
        ImportMetamodelCommand(),
        ExportMetamodelCommand(),
        ImportModelCommand(),
        ExportModelCommand(),
    )

fun main(args: Array<String>) = createLionWebEMFCommand().main(args)
