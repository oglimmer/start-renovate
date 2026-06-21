/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/**
 * A repository's Renovate config, fetched on behalf of the authenticated user so the SPA can load
 * it into the generator's editor. Unlike the public URL import, this goes through the user's OAuth
 * token and therefore works for private repos and for GitLab (whose raw endpoint blocks browser
 * CORS).
 *
 * @param fullName owner/name (GitLab subgroups included)
 * @param configFilePath which file the config came from
 * @param json the config serialized as pretty-printed JSON, ready to drop into the editor
 */
public record RepoConfigResponse(String fullName, String configFilePath, String json) {}
