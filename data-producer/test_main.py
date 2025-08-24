import os
import pytest
from unittest.mock import patch, MagicMock, mock_open
import main

def test_delivery_report_success():
    with patch('builtins.print') as mock_print:
        main.delivery_report(None, 'msg-metadata')
        mock_print.assert_called_with("record produced to msg='msg-metadata'")

def test_delivery_report_error():
    with patch('builtins.print') as mock_print:
        main.delivery_report('error!', None)
        mock_print.assert_called_with('delivery failed: error!')

@patch('main.Producer')
def test_kafka_producer_env_vars(mock_producer):
    os.environ['KAFKA_BOOTSTRAP_SERVERS'] = 'test:1234'
    os.environ['KAFKA_SECURITY_PROTOCOL'] = 'PLAINTEXT'
    os.environ['KAFKA_SSL_CA_LOCATION'] = '/tmp/ca.crt'
    os.environ['KAFKA_CLIENT_ID'] = 'test-client'
    os.environ['KAFKA_ENABLE_IDEMPOTENCE'] = 'False'

    mock_instance = MagicMock()
    mock_producer.return_value = mock_instance

    main.kafka_producer('topic', 'message', 'key')
    mock_producer.assert_called_with({
        'bootstrap.servers': 'test:1234',
        'security.protocol': 'PLAINTEXT',
        'ssl.ca.location': '/tmp/ca.crt',
        'client.id': 'test-client',
        'enable.idempotence': 'False',
    })
    mock_instance.produce.assert_called_with(
        topic='topic', key='key', value='message', callback=main.delivery_report
    )
    mock_instance.flush.assert_called_with(10)

@patch('main.kafka_producer')
def test_main_reads_file_and_calls_producer(mock_kafka_producer):
    m = mock_open(read_data='order1\norder2\n')
    with patch('builtins.open', m):
        with patch('builtins.print'):
            main.main()
    mock_kafka_producer.assert_any_call('local-input-topic', 'order1', 'orders')
    mock_kafka_producer.assert_any_call('local-input-topic', 'order2', 'orders')
