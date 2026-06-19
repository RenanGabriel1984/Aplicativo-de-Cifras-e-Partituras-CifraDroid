package com.example.util

import com.example.data.Repertoire
import org.json.JSONArray
import org.json.JSONObject

object RepertoireUtil {

    data class RepertoireCategory(
        val name: String,
        val manuscriptIds: MutableList<Int>
    )

    fun getFlatManuscriptIds(repertoire: Repertoire): List<Int> {
        return try {
            val json = repertoire.manuscriptIdsJson.trim()
            if (json.startsWith("{")) {
                val obj = JSONObject(json)
                val cats = obj.optJSONArray("categories") ?: JSONArray()
                val list = mutableListOf<Int>()
                for (i in 0 until cats.length()) {
                    val catObj = cats.optJSONObject(i) ?: continue
                    val items = catObj.optJSONArray("items") ?: JSONArray()
                    for (j in 0 until items.length()) {
                        list.add(items.getInt(j))
                    }
                }
                list
            } else {
                val arr = JSONArray(json)
                List(arr.length()) { arr.getInt(it) }
            }
        } catch(e: Exception) {
            emptyList()
        }
    }

    fun getCategories(repertoire: Repertoire): List<RepertoireCategory> {
        return try {
            val json = repertoire.manuscriptIdsJson.trim()
            if (json.startsWith("{")) {
                val obj = JSONObject(json)
                val cats = obj.optJSONArray("categories") ?: JSONArray()
                val list = mutableListOf<RepertoireCategory>()
                for (i in 0 until cats.length()) {
                    val catObj = cats.optJSONObject(i) ?: continue
                    val name = catObj.optString("name", "Sem Categoria")
                    val items = catObj.optJSONArray("items") ?: JSONArray()
                    val ids = mutableListOf<Int>()
                    for (j in 0 until items.length()) {
                        ids.add(items.getInt(j))
                    }
                    list.add(RepertoireCategory(name, ids))
                }
                list
            } else {
                // Legacy format: create a default category
                val arr = JSONArray(json)
                val ids = mutableListOf<Int>()
                for (i in 0 until arr.length()) {
                    ids.add(arr.getInt(i))
                }
                listOf(RepertoireCategory("Principal", ids))
            }
        } catch(e: Exception) {
            emptyList()
        }
    }

    fun toJson(categories: List<RepertoireCategory>): String {
        val root = JSONObject()
        val catsArray = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("name", cat.name)
            val itemsArray = JSONArray()
            for (id in cat.manuscriptIds) {
                itemsArray.put(id)
            }
            catObj.put("items", itemsArray)
            catsArray.put(catObj)
        }
        root.put("categories", catsArray)
        return root.toString()
    }
}
