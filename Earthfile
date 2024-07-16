VERSION 0.8

ARG --global ALL_BUILD_TARGETS="backend"

ARG --global DOCKER_IMAGE_PREFIX="cf-lob"
ARG --global DOCKER_IMAGES_EXTRA_TAGS=""
ARG --global DOCKER_REGISTRIES="hub.docker.com"
ARG --global HUB_DOCKER_COM_ORG=cardanofoundation

all:
  LOCALLY
  ARG RELEASE_TAG
  FOR image_target IN $ALL_BUILD_TARGETS
    BUILD +${image_target} --RELEASE_TAG=${RELEASE_TAG}
  END

docker-publish:
  ARG EARTHLY_GIT_SHORT_HASH
  ARG RELEASE_TAG
  WAIT
    BUILD +all --RELEASE_TAG=${RELEASE_TAG}
  END
  LOCALLY
  LET IMAGE_NAME = ""
  FOR registry IN $DOCKER_REGISTRIES
    FOR image_target IN $ALL_BUILD_TARGETS
      SET IMAGE_NAME = ${DOCKER_IMAGE_PREFIX}-${image_target}
      IF [ ! -z "$DOCKER_IMAGES_EXTRA_TAGS" ]
        FOR image_tag IN $DOCKER_IMAGES_EXTRA_TAGS
          IF [ "$registry" = "hub.docker.com" ]
            RUN echo docker tag ${IMAGE_NAME}:latest ${HUB_DOCKER_COM_ORG}/${IMAGE_NAME}:${image_tag}
            RUN echo docker push ${HUB_DOCKER_COM_ORG}/${IMAGE_NAME}:${image_tag}
          ELSE
            RUN echo docker tag ${IMAGE_NAME}:latest ${registry}/${IMAGE_NAME}:${image_tag}
            RUN echo docker push ${registry}/${IMAGE_NAME}:${image_tag}
          END
        END
      END
      IF [ "$registry" = "hub.docker.com" ]
        RUN echo docker tag ${IMAGE_NAME}:latest ${HUB_DOCKER_COM_ORG}/${IMAGE_NAME}:${EARTHLY_GIT_SHORT_HASH}
        RUN echo docker push ${HUB_DOCKER_COM_ORG}/${IMAGE_NAME}:${EARTHLY_GIT_SHORT_HASH}
      ELSE
        RUN echo docker tag ${IMAGE_NAME}:latest ${registry}/${IMAGE_NAME}:${EARTHLY_GIT_SHORT_HASH}
        RUN echo docker push ${registry}/${IMAGE_NAME}:${EARTHLY_GIT_SHORT_HASH}
      END
    END
  END

backend:
  ARG EARTHLY_TARGET_NAME
  FROM DOCKERFILE -f Dockerfile --target ${EARTHLY_TARGET_NAME} .
  SAVE IMAGE ${DOCKER_IMAGE_PREFIX}-${EARTHLY_TARGET_NAME}:latest