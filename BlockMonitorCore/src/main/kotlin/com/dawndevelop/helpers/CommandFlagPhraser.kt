package com.dawndevelop.helpers

class CommandFlagPhraser {

    companion object {
        public fun HandleArguments(args: Array<String>){
            val argMap: MutableMap<String, String> = mutableMapOf()
            for (arg in  args){
                val argSplit = arg.split("=")
                val argOne = argSplit[0].replace("-", "")
                val argTwo = argSplit[1]
                argMap.put(argOne, argTwo)
            }
        }
    }
}