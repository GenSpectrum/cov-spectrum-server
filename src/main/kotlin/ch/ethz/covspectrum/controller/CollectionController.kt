package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.SpectrumCollection
import ch.ethz.covspectrum.entity.res.AddCollectionResponse
import ch.ethz.covspectrum.service.DatabaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/resource/collection")
class CollectionController(
    private val databaseService: DatabaseService
) {
    @GetMapping("")
    fun getCollections(@RequestParam(defaultValue = "true") fetchVariants: Boolean): List<SpectrumCollection> {
        return databaseService.getCollections(fetchVariants)
    }

    @PostMapping("")
    fun addCollection(@RequestBody collection: SpectrumCollection): AddCollectionResponse {
        check(collection.id == null) // TODO Send better error message
        val (id, adminKey) = databaseService.insertCollection(collection)
        return AddCollectionResponse(id, adminKey)
    }

    @GetMapping("/{id}")
    fun getCollection(@PathVariable id: Int): ResponseEntity<SpectrumCollection> {
        val collection = databaseService.getCollection(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok().body(collection)
    }

    @PutMapping("/{id}")
    fun updateCollection(
        @PathVariable id: Int,
        @RequestBody collection: SpectrumCollection,
        adminKey: String?
    ): ResponseEntity<Void> {
        check(collection.id == null || collection.id == id)
        collection.id = id
        if (adminKey == null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        val keyValid = databaseService.validateCollectionAdminKey(id, adminKey)
        if (keyValid == null) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        } else if (!keyValid) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        databaseService.updateCollection(collection, adminKey)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCollection(
        @PathVariable id: Int,
        adminKey: String?
    ): ResponseEntity<Void> {
        if (adminKey == null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        val keyValid = databaseService.validateCollectionAdminKey(id, adminKey)
        if (keyValid == null) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        } else if (!keyValid) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        databaseService.deleteCollection(id)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/{id}/validate-admin-key")
    fun validateAdminKey(
        @PathVariable id: Int,
        @RequestBody adminKey: String?
    ): ResponseEntity<Boolean> {
        if (adminKey == null) {
            return ResponseEntity.ok().body(false)
        }
        val keyValid = databaseService.validateCollectionAdminKey(id, adminKey)
        if (keyValid == null) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity.ok().body(keyValid)
    }
}
