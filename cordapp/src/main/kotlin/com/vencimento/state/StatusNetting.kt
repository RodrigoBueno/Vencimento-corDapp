package com.vencimento.state

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class StatusNetting {
    Proposto,
    Recusado,
    Aceito
}