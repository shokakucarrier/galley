/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.internal.xfer;

import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ExistenceHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    public ExistenceHandler()
    {
    }

    public ExistenceHandler( final NotFoundCache nfc )
    {
        this.nfc = nfc;
    }

    public boolean exists( final ConcreteResource resource, final int timeoutSeconds, final Transport transport, final boolean suppressFailures )
        throws TransferException
    {
        if ( nfc.isMissing( resource ) )
        {
            logger.debug( "NFC: Already marked as missing: {}", resource );
            return false;
        }

        if ( transport == null )
        {
            logger.warn( "No transports available to handle: {} with location type: {}", resource,
                         resource.getLocation()
                                 .getClass()
                                 .getSimpleName() );
            return false;
        }

        logger.debug( "EXISTS {}", resource );

        final ExistenceJob job = transport.createExistenceJob( resource, timeoutSeconds );

        // TODO: execute this stuff in a thread just like downloads/publishes. Requires cache storage...
        try
        {
            final Boolean result = job.call();

            if ( job.getError() != null )
            {
                logger.debug( "NFC: Download error. Marking as missing: {}", resource );
                nfc.addMissing( resource );

                if ( !suppressFailures )
                {
                    throw job.getError();
                }
            }
            else if ( result == null )
            {
                logger.debug( "NFC: Download did not complete. Marking as missing: {}", resource );
                nfc.addMissing( resource );
            }
            else if ( !result )
            {
                logger.debug( "NFC: Existence check returned false. Marking as missing: {}", resource );
                nfc.addMissing( resource );
            }

            return result;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        catch ( final Exception e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed listing: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }

        return false;
    }

}
