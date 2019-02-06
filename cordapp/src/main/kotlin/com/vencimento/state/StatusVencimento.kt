package com.vencimento.state

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class StatusVencimento {

    Emitido,
    Recusado,
    APagar,
    Pago

}